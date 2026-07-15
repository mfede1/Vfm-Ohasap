import UIKit

protocol SimulationViewDelegate: AnyObject {
    func simulationView(_ view: SimulationView, selected object: PhysicsObject?)
    func simulationViewDidChange(_ view: SimulationView)
    func simulationView(_ view: SimulationView, requestInspector object: PhysicsObject)
    func simulationView(_ view: SimulationView, requestFineCut object: PhysicsObject)
    func simulationView(_ view: SimulationView, requestVectorFor object: PhysicsObject, force: Bool, magnitude: CGFloat, angle: CGFloat)
}

final class SimulationView: UIView {
    let engine: PhysicsEngine
    weak var delegate: SimulationViewDelegate?
    var mode: ToolMode = .normal { didSet { setNeedsDisplay() } }
    var selectedObject: PhysicsObject? { didSet { setNeedsDisplay() } }
    var previewStart: CGPoint?
    var previewEnd: CGPoint?
    var isRunning = false
    var currentCutPercentage: CGFloat = 0
    var fineCutEnabled = false
    private var dragOffset = CGVector.zero
    private var touchStart: CGPoint?
    private var lastTouch: CGPoint?
    private var displayLink: CADisplayLink?
    private var previousTimestamp: CFTimeInterval = 0
    private var accumulator: CGFloat = 0
    private var wrapDrawingRope: PhysicsObject?
    private var wrapDrawingLength: CGFloat = 0
    private var connectingObject: PhysicsObject?
    private var connectingEndA = true
    private weak var connectionHighlight: PhysicsObject?
    private var selectingPulley: PhysicsObject?
    private var pendingRotationObject: PhysicsObject?
    private var rotatingObject: PhysicsObject?
    private var rotationPivot = CGPoint.zero
    private var rotationPivotIsA = true
    private weak var rotatingFollowObject: PhysicsObject?
    private weak var conditionTargetOwner: PhysicsObject?
    private weak var conditionHighlightObject: PhysicsObject?
    private var conditionTargetCompletion: ((PhysicsObject?) -> Void)?
    private var palettePreviewKind: ObjectKind?
    private var palettePreviewPoint = CGPoint.zero

    init(engine: PhysicsEngine) {
        self.engine = engine
        super.init(frame: .zero)
        backgroundColor = .white
        isMultipleTouchEnabled = true
        let longPress = UILongPressGestureRecognizer(target: self, action: #selector(longPressed(_:)))
        longPress.minimumPressDuration = 0.45
        longPress.cancelsTouchesInView = false
        longPress.delaysTouchesBegan = false
        addGestureRecognizer(longPress)
        displayLink = CADisplayLink(target: self, selector: #selector(frameTick(_:)))
        displayLink?.add(to: .main, forMode: .common)
    }

    required init?(coder: NSCoder) { nil }

    deinit { displayLink?.invalidate() }

    override func layoutSubviews() {
        super.layoutSubviews()
        engine.updateWorld(size: bounds.size)
    }

    @objc private func frameTick(_ link: CADisplayLink) {
        guard isRunning else { previousTimestamp = link.timestamp; return }
        if previousTimestamp == 0 { previousTimestamp = link.timestamp; return }
        let elapsed = min(0.05, link.timestamp - previousTimestamp)
        previousTimestamp = link.timestamp
        accumulator += CGFloat(elapsed)
        while accumulator >= PhysicsEngine.fixedTimeStep {
            engine.step(PhysicsEngine.fixedTimeStep)
            engine.elapsedTime += PhysicsEngine.fixedTimeStep
            engine.saveStateIfNeeded()
            accumulator -= PhysicsEngine.fixedTimeStep
            if engine.elapsedTime >= 60 { engine.elapsedTime = 60; isRunning = false; break }
        }
        delegate?.simulationViewDidChange(self)
        setNeedsDisplay()
    }

    @objc private func longPressed(_ recognizer: UILongPressGestureRecognizer) {
        guard recognizer.state == .began else { return }
        guard mode == .normal, rotatingObject == nil, wrapDrawingRope == nil, connectingObject == nil, selectingPulley == nil, conditionTargetCompletion == nil else { return }
        let point = recognizer.location(in: self)
        guard let object = engine.hitTest(point) else { return }
        if pendingRotationObject === object {
            startRotation(object, touch: point)
            pendingRotationObject = nil
            setNeedsDisplay()
            return
        }
        selectedObject = object
        delegate?.simulationView(self, selected: object)
        delegate?.simulationView(self, requestInspector: object)
    }

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        guard let point = touches.first?.location(in: self) else { return }
        if let completion = conditionTargetCompletion {
            let hit = engine.hitTest(point, prioritizeRods: false)
            let target = hit !== conditionTargetOwner ? hit : nil
            conditionHighlightObject = target
            conditionTargetOwner = nil
            conditionTargetCompletion = nil
            setNeedsDisplay()
            DispatchQueue.main.async { completion(target) }
            return
        }
        if let pending = pendingRotationObject {
            if engine.hitTest(point) === pending { startRotation(pending, touch: point) }
            pendingRotationObject = nil
            setNeedsDisplay()
            return
        }
        if let pulley = selectingPulley {
            if let ramp = engine.hitTest(point, prioritizeRods: false), ramp.kind == .straightRamp {
                pulley.pulleyRamp = ramp
                pulley.fixed = true
                pulley.position = CGPoint(x: ramp.position.x + (ramp.horizontal ? ramp.size.width * 0.5 : -ramp.size.width * 0.5), y: ramp.position.y - ramp.size.height * 0.5)
                pulley.initialPosition = pulley.position
            } else if engine.hitTest(point, prioritizeRods: false) == nil { pulley.pulleyRamp = nil }
            selectingPulley = nil
            delegate?.simulationViewDidChange(self)
            setNeedsDisplay()
            return
        }
        if let link = connectingObject {
            completeConnection(link, at: point)
            return
        }
        if let rope = wrapDrawingRope {
            rope.wrapPoints = [point]
            wrapDrawingLength = 0
            rope.ropeParticles.removeAll()
            rope.ropeParticlesInitialized = false
            setNeedsDisplay()
            return
        }
        touchStart = point
        lastTouch = point
        previewStart = point
        previewEnd = point
        switch mode {
        case .normal:
            selectedObject = engine.hitTest(point)
            if let selectedObject { dragOffset = selectedObject.position - point }
            delegate?.simulationView(self, selected: selectedObject)
        case .velocity, .force:
            if let hit = engine.hitTest(point), !engine.isConstraintBody(hit) { selectedObject = hit } else { selectedObject = nil }
            delegate?.simulationView(self, selected: selectedObject)
        case .erase:
            engine.erase(at: point)
            delegate?.simulationViewDidChange(self)
        case .draw, .cut:
            break
        }
        setNeedsDisplay()
    }

    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
        guard let point = touches.first?.location(in: self) else { return }
        if let rotating = rotatingObject {
            handleRotation(rotating, touch: point)
            setNeedsDisplay()
            return
        }
        if let rope = wrapDrawingRope {
            if let last = rope.wrapPoints.last {
                let segment = hypot(point.x - last.x, point.y - last.y)
                if segment >= 8, wrapDrawingLength + segment <= rope.ropeLength { rope.wrapPoints.append(point); wrapDrawingLength += segment }
            } else {
                rope.wrapPoints.append(point)
                wrapDrawingLength = 0
            }
            setNeedsDisplay()
            return
        }
        guard let start = touchStart else { return }
        previewEnd = mode == .draw ? CGPoint(x: point.x, y: start.y) : point
        switch mode {
        case .normal:
            if let object = selectedObject, !isRunning {
                object.position = point + dragOffset
                object.initialPosition = object.position
                if object.kind == .ring, let rod = object.ringHostRod {
                    let ends = engine.endpoints(of: rod)
                    let axis = ends.1 - ends.0
                    let lengthSquared = axis.dx * axis.dx + axis.dy * axis.dy
                    object.ringParameter = lengthSquared > 0 ? max(0, min(1, ((point.x - ends.0.x) * axis.dx + (point.y - ends.0.y) * axis.dy) / lengthSquared)) : 0
                }
                delegate?.simulationViewDidChange(self)
            }
        case .erase:
            engine.erase(at: point)
            delegate?.simulationViewDidChange(self)
        case .cut:
            if let object = engine.findCutTarget(from: start, to: point) {
                currentCutPercentage = engine.cutPercentage(object: object, from: start, to: point)
            }
        case .velocity, .force, .draw:
            break
        }
        lastTouch = point
        setNeedsDisplay()
    }

    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        let point = touches.first?.location(in: self) ?? lastTouch ?? .zero
        if rotatingObject != nil {
            rotatingObject = nil
            rotatingFollowObject = nil
            delegate?.simulationViewDidChange(self)
            setNeedsDisplay()
            return
        }
        if let rope = wrapDrawingRope {
            rope.ropeWrapped = rope.wrapPoints.count >= 2
            rope.ropeParticlesInitialized = false
            wrapDrawingRope = nil
            wrapDrawingLength = 0
            touchStart = nil
            lastTouch = nil
            delegate?.simulationViewDidChange(self)
            setNeedsDisplay()
            return
        }
        guard let start = touchStart else { return }
        switch mode {
        case .velocity:
            if let object = selectedObject {
                let vector = point - start
                let magnitude = min(15, vector.length / engine.pixelsPerMeter * 2)
                var angle = atan2(-vector.dy, vector.dx) * 180 / .pi
                if angle < 0 { angle += 360 }
                delegate?.simulationView(self, requestVectorFor: object, force: false, magnitude: (magnitude * 10).rounded() / 10, angle: angle.rounded())
            }
        case .force:
            if let object = selectedObject {
                let vector = point - start
                let magnitude = min(50, vector.length * 20 / engine.pixelsPerMeter)
                var angle = atan2(-vector.dy, vector.dx) * 180 / .pi
                if angle < 0 { angle += 360 }
                delegate?.simulationView(self, requestVectorFor: object, force: true, magnitude: (magnitude * 10).rounded() / 10, angle: angle.rounded())
            }
        case .draw:
            if hypot(point.x - start.x, point.y - start.y) > 10 { engine.addCeiling(from: start, to: point) }
        case .cut:
            if fineCutEnabled, let target = engine.findCutTarget(from: start, to: point), target.kind != .rod {
                delegate?.simulationView(self, requestFineCut: target)
            } else {
                engine.cut(from: start, to: point)
            }
        case .normal, .erase:
            break
        }
        previewStart = nil
        previewEnd = nil
        touchStart = nil
        lastTouch = nil
        currentCutPercentage = 0
        delegate?.simulationViewDidChange(self)
        setNeedsDisplay()
    }

    override func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
        if let rope = wrapDrawingRope {
            rope.ropeWrapped = rope.wrapPoints.count >= 2
            rope.ropeParticlesInitialized = false
            wrapDrawingRope = nil
            wrapDrawingLength = 0
        }
        rotatingObject = nil
        rotatingFollowObject = nil
        previewStart = nil
        previewEnd = nil
        touchStart = nil
        lastTouch = nil
        setNeedsDisplay()
    }

    override func draw(_ rect: CGRect) {
        guard let context = UIGraphicsGetCurrentContext() else { return }
        context.setFillColor(UIColor.white.cgColor)
        context.fill(bounds)
        drawWorld(context)
        for object in engine.objects {
            if object.showTrajectory { drawTrajectory(object, context) }
            drawObject(object, context)
            if object === conditionHighlightObject { drawConditionHighlight(object, context) }
            let linkedRodShowsForces = engine.objects.contains { rod in
                guard rod.kind == .rod, rod.showForces else { return false }
                if case .object(let id) = rod.anchorA, id == object.id { return true }
                if case .object(let id) = rod.anchorB, id == object.id { return true }
                return false
            }
            if object.showForces || linkedRodShowsForces { drawForces(object, context) }
            if object.showState { drawState(object, context) }
        }
        if let kind = palettePreviewKind {
            let ghost = engine.createObject(kind, at: palettePreviewPoint)
            context.saveGState()
            context.setAlpha(0.35)
            drawObject(ghost, context)
            context.restoreGState()
        }
        if let link = connectingObject {
            let text = connectingEndA ? "点击物体/天花板/地板/墙壁绑定端点A" : "点击物体/天花板/地板/墙壁绑定端点B"
            text.draw(at: CGPoint(x: 20, y: 14), withAttributes: [.font: UIFont.systemFont(ofSize: 17, weight: .medium), .foregroundColor: UIColor.systemBlue])
            if let end = previewEnd {
                let endpoints = engine.endpoints(of: link)
                let start = connectingEndA ? endpoints.1 : endpoints.0
                let context = UIGraphicsGetCurrentContext()
                context?.setStrokeColor(UIColor.systemBlue.withAlphaComponent(0.55).cgColor)
                context?.setLineWidth(1.5)
                context?.move(to: start)
                context?.addLine(to: end)
                context?.strokePath()
            }
        }
        if selectingPulley != nil {
            "点击直斜面以绑定（点击空白取消）".draw(at: CGPoint(x: 20, y: 14), withAttributes: [.font: UIFont.systemFont(ofSize: 17, weight: .medium), .foregroundColor: UIColor.systemBlue])
        }
        if conditionTargetCompletion != nil {
            "点击物体选择条件目标（点击空白取消）".draw(at: CGPoint(x: 20, y: 14), withAttributes: [.font: UIFont.systemFont(ofSize: 17, weight: .medium), .foregroundColor: UIColor.systemOrange])
        }
        if pendingRotationObject != nil {
            "长按弹簧/绳子以旋转".draw(at: CGPoint(x: 20, y: 14), withAttributes: [.font: UIFont.systemFont(ofSize: 17, weight: .medium), .foregroundColor: UIColor.systemBlue])
        }
        if let rotating = rotatingObject {
            var degrees = rotating.rotation * 180 / .pi
            if degrees < 0 { degrees += 360 }
            (SurdFormatter.text(degrees, places: engine.decimalPlaces) + "°").draw(at: CGPoint(x: rotating.position.x + 10, y: rotating.position.y - 28), withAttributes: [.font: UIFont.systemFont(ofSize: 18, weight: .medium), .foregroundColor: UIColor.systemBlue])
        }
        if mode == .normal, touchStart != nil, let selectedObject, ![.spring, .rope, .rod].contains(selectedObject.kind) { drawDragMeasurements(selectedObject, context: context) }
        drawPreview(context)
        if let rope = wrapDrawingRope, rope.wrapPoints.count > 1 {
            context.setStrokeColor(UIColor.systemOrange.cgColor)
            context.setLineWidth(3)
            context.addLines(between: rope.wrapPoints)
            context.strokePath()
        }
    }

    func beginWrapDrawing(_ rope: PhysicsObject) {
        isRunning = false
        wrapDrawingRope = rope
        rope.ropeWrapped = false
        rope.wrapPoints.removeAll()
        rope.ropeParticles.removeAll()
        rope.ropeParticlesInitialized = false
        wrapDrawingLength = 0
    }

    func showPalettePreview(kind: ObjectKind?, at point: CGPoint) {
        palettePreviewKind = kind
        palettePreviewPoint = point
        setNeedsDisplay()
    }

    func beginConnection(_ object: PhysicsObject, endA: Bool) {
        isRunning = false
        connectingObject = object
        connectingEndA = endA
        connectionHighlight = nil
        previewEnd = object.position
        setNeedsDisplay()
    }

    func beginPulleyRampSelection(_ pulley: PhysicsObject) {
        isRunning = false
        selectingPulley = pulley
        setNeedsDisplay()
    }

    func beginRotation(_ object: PhysicsObject) {
        isRunning = false
        pendingRotationObject = object
        setNeedsDisplay()
    }

    func beginConditionTargetSelection(owner: PhysicsObject, completion: @escaping (PhysicsObject?) -> Void) {
        isRunning = false
        conditionTargetOwner = owner
        conditionTargetCompletion = completion
        setNeedsDisplay()
    }

    private func drawConditionHighlight(_ object: PhysicsObject, _ context: CGContext) {
        context.saveGState()
        context.setStrokeColor(UIColor.systemOrange.cgColor)
        context.setLineWidth(3)
        if object.kind == .spring || object.kind == .rope || object.kind == .rod {
            if object.kind == .rope, object.ropeWrapped, object.wrapPoints.count > 1 {
                context.addLines(between: object.wrapPoints)
            } else {
                let ends = engine.endpoints(of: object)
                context.move(to: ends.0)
                context.addLine(to: ends.1)
            }
            context.strokePath()
        } else if object.kind == .ball || object.kind == .pulley || object.kind == .ring {
            context.strokeEllipse(in: CGRect(x: object.position.x - object.radius - 5, y: object.position.y - object.radius - 5, width: object.radius * 2 + 10, height: object.radius * 2 + 10))
        } else {
            context.translateBy(x: object.position.x, y: object.position.y)
            context.rotate(by: object.rotation)
            context.stroke(CGRect(x: -object.size.width * 0.5 - 5, y: -object.size.height * 0.5 - 5, width: object.size.width + 10, height: object.size.height + 10))
        }
        context.restoreGState()
    }

    private func startRotation(_ object: PhysicsObject, touch: CGPoint) {
        guard !object.angleFixed else { return }
        let endpoints = engine.endpoints(of: object)
        let pivotIsA = (touch - endpoints.0).length > (touch - endpoints.1).length
        rotationPivotIsA = pivotIsA
        rotationPivot = pivotIsA ? endpoints.0 : endpoints.1
        let movingAnchor = pivotIsA ? object.anchorB : object.anchorA
        if case .object(let id) = movingAnchor { rotatingFollowObject = engine.objects.first { $0.id == id && !$0.fixed } }
        else { rotatingFollowObject = nil }
        rotatingObject = object
    }

    private func handleRotation(_ object: PhysicsObject, touch: CGPoint) {
        guard !object.angleFixed else { return }
        let angle = atan2(touch.y - rotationPivot.y, touch.x - rotationPivot.x)
        let length = object.kind == .spring ? object.naturalLength : (object.kind == .rod ? object.rodLength : object.ropeLength)
        let direction = CGVector(dx: cos(angle), dy: sin(angle))
        object.position = rotationPivot + direction * (length * 0.5)
        object.rotation = rotationPivotIsA ? angle : angle + .pi
        object.horizontal = true
        object.size = CGSize(width: length, height: object.kind == .spring ? 20 : (object.kind == .rod ? 6 : 10))
        rotatingFollowObject?.position = rotationPivot + direction * length
    }

    private func completeConnection(_ link: PhysicsObject, at point: CGPoint) {
        if abs(point.y - engine.groundY) < 20, link.horizontal { setNeedsDisplay(); return }
        if (abs(point.x - engine.wallLeftX) < 20 || abs(point.x - engine.wallRightX) < 20), !link.horizontal { setNeedsDisplay(); return }
        let endpoint: Anchor?
        if let hit = engine.hitTest(point, prioritizeRods: false), hit !== link, !engine.isConstraintBody(hit), !(hit.kind == .ring && hit.ringHostRod === link) {
            if connectionHighlight !== hit {
                connectionHighlight = hit
                setNeedsDisplay()
                return
            }
            endpoint = .object(hit.id)
        } else if let ceiling = engine.ceilingIndex(at: point) {
            endpoint = .ceiling(ceiling, point.x)
        } else if abs(point.y - engine.groundY) < 20, !link.horizontal {
            endpoint = .ground(point.x)
        } else if abs(point.x - engine.wallLeftX) < 20, point.y >= 0, point.y <= engine.groundY, link.horizontal {
            endpoint = .leftWall(point.y)
        } else if abs(point.x - engine.wallRightX) < 20, point.y >= 0, point.y <= engine.groundY, link.horizontal {
            endpoint = .rightWall(point.y)
        } else {
            endpoint = nil
        }
        if let endpoint {
            engine.setAnchor(endpoint, on: link, endA: connectingEndA)
            engine.refreshConstraintPose(link)
        }
        connectingObject = nil
        connectionHighlight = nil
        previewEnd = nil
        delegate?.simulationViewDidChange(self)
        setNeedsDisplay()
    }

    private func drawWorld(_ context: CGContext) {
        context.setStrokeColor(UIColor(white: 0.2, alpha: 1).cgColor)
        context.setLineWidth(2)
        context.move(to: CGPoint(x: engine.wallLeftX, y: 0))
        context.addLine(to: CGPoint(x: engine.wallLeftX, y: engine.groundY))
        context.move(to: CGPoint(x: engine.wallRightX, y: 0))
        context.addLine(to: CGPoint(x: engine.wallRightX, y: engine.groundY))
        context.move(to: CGPoint(x: 0, y: engine.groundY))
        context.addLine(to: CGPoint(x: bounds.width, y: engine.groundY))
        context.strokePath()
        context.setLineWidth(1)
        for x in stride(from: CGFloat(0), through: bounds.width, by: 12) where !engine.groundEraseRanges.contains(where: { $0.contains(x) }) {
            context.move(to: CGPoint(x: x, y: engine.groundY))
            context.addLine(to: CGPoint(x: x - 8, y: engine.groundY + 10))
        }
        for y in stride(from: CGFloat(20), through: engine.groundY, by: 12) {
            context.move(to: CGPoint(x: engine.wallLeftX, y: y))
            context.addLine(to: CGPoint(x: engine.wallLeftX + 9, y: y - 7))
            context.move(to: CGPoint(x: engine.wallRightX, y: y))
            context.addLine(to: CGPoint(x: engine.wallRightX - 9, y: y - 7))
        }
        context.strokePath()
        context.setStrokeColor(UIColor.darkGray.cgColor)
        context.setLineWidth(3)
        for segment in engine.ceilings {
            context.move(to: segment.start)
            context.addLine(to: segment.end)
            context.strokePath()
            for x in stride(from: min(segment.start.x, segment.end.x), to: max(segment.start.x, segment.end.x), by: 12) {
                context.move(to: CGPoint(x: x, y: segment.start.y))
                context.addLine(to: CGPoint(x: x + 8, y: segment.start.y - 10))
            }
            context.strokePath()
        }
    }

    private func drawObject(_ object: PhysicsObject, _ context: CGContext) {
        if object.kind == .rope, object.ropeWrapped, object.wrapPoints.count > 1 {
            context.setStrokeColor(UIColor.black.cgColor)
            context.setLineWidth(2.5)
            context.addLines(between: object.wrapPoints)
            context.strokePath()
            return
        }
        context.saveGState()
        context.translateBy(x: object.position.x, y: object.position.y)
        context.rotate(by: object.rotation)
        context.setStrokeColor(UIColor.black.cgColor)
        context.setFillColor(UIColor.white.cgColor)
        context.setLineWidth(2)
        switch object.kind {
        case .block, .board:
            drawPolygonOrRect(object, context)
            context.setStrokeColor(UIColor(white: 0.75, alpha: 1).cgColor)
            context.setLineWidth(1)
            if object.kind == .block {
                for y in stride(from: -object.size.height * 0.5 + 8, to: object.size.height * 0.5, by: 8) {
                    context.move(to: CGPoint(x: -object.size.width * 0.5 + 2, y: y))
                    context.addLine(to: CGPoint(x: object.size.width * 0.5 - 2, y: y))
                }
            } else {
                context.move(to: CGPoint(x: -object.size.width * 0.5 + 2, y: 0))
                context.addLine(to: CGPoint(x: object.size.width * 0.5 - 2, y: 0))
            }
            context.strokePath()
        case .ball:
            if object.polygon.isEmpty {
                let circle = CGRect(x: -object.radius, y: -object.radius, width: object.radius * 2, height: object.radius * 2)
                context.setFillColor(UIColor.orange.cgColor)
                context.fillEllipse(in: circle)
                context.strokeEllipse(in: circle)
                context.setFillColor(UIColor.white.withAlphaComponent(0.38).cgColor)
                context.fillEllipse(in: CGRect(x: -object.radius * 0.65, y: -object.radius * 0.65, width: object.radius * 0.65, height: object.radius * 0.65))
            } else { drawPolygonOrRect(object, context) }
        case .spring:
            drawSpring(object, context)
        case .rope:
            context.setLineWidth(2.5)
            context.move(to: CGPoint(x: -object.size.width * 0.5, y: 0))
            context.addLine(to: CGPoint(x: object.size.width * 0.5, y: 0))
            context.strokePath()
            context.fillEllipse(in: CGRect(x: -object.size.width * 0.5 - 2.5, y: -2.5, width: 5, height: 5))
            context.fillEllipse(in: CGRect(x: object.size.width * 0.5 - 2.5, y: -2.5, width: 5, height: 5))
        case .straightRamp:
            let width = object.size.width * 0.5
            let height = object.size.height * 0.5
            context.beginPath()
            if object.horizontal {
                context.move(to: CGPoint(x: -width, y: height))
                context.addLine(to: CGPoint(x: width, y: height))
                context.addLine(to: CGPoint(x: width, y: -height))
            } else {
                context.move(to: CGPoint(x: width, y: height))
                context.addLine(to: CGPoint(x: -width, y: height))
                context.addLine(to: CGPoint(x: -width, y: -height))
            }
            context.closePath()
            context.drawPath(using: .fillStroke)
        case .arcRamp:
            drawArcRamp(object, context)
        case .container:
            drawContainer(object, context)
        case .pulley:
            context.fillEllipse(in: CGRect(x: -object.radius, y: -object.radius, width: object.radius * 2, height: object.radius * 2))
            context.strokeEllipse(in: CGRect(x: -object.radius, y: -object.radius, width: object.radius * 2, height: object.radius * 2))
            context.setStrokeColor(UIColor.gray.cgColor)
            context.setLineWidth(3)
            context.strokeEllipse(in: CGRect(x: -object.radius * 0.7, y: -object.radius * 0.7, width: object.radius * 1.4, height: object.radius * 1.4))
            context.setFillColor(UIColor.darkGray.cgColor)
            context.fillEllipse(in: CGRect(x: -object.radius * 0.18, y: -object.radius * 0.18, width: object.radius * 0.36, height: object.radius * 0.36))
        case .rod:
            let height = max(6, object.realHeight * engine.pixelsPerMeter)
            let rect = CGRect(x: -object.rodLength * 0.5, y: -height * 0.5, width: object.rodLength, height: height)
            context.beginPath()
            context.addRect(rect)
            context.drawPath(using: .fillStroke)
            context.fillEllipse(in: CGRect(x: -object.rodLength * 0.5 - 3, y: -3, width: 6, height: 6))
            context.fillEllipse(in: CGRect(x: object.rodLength * 0.5 - 3, y: -3, width: 6, height: 6))
        case .ring:
            context.setStrokeColor(UIColor(white: 0.25, alpha: 1).cgColor)
            context.setLineWidth(4)
            context.strokeEllipse(in: CGRect(x: -object.radius, y: -object.radius, width: object.radius * 2, height: object.radius * 2))
        }
        if object === selectedObject {
            context.setStrokeColor(UIColor.systemBlue.withAlphaComponent(0.75).cgColor)
            context.setLineWidth(2)
            context.setLineDash(phase: 0, lengths: [5, 3])
            context.stroke(CGRect(x: -object.size.width * 0.5 - 5, y: -object.size.height * 0.5 - 5, width: object.size.width + 10, height: object.size.height + 10))
        }
        if object === connectionHighlight {
            context.setLineDash(phase: 0, lengths: [])
            context.setStrokeColor(UIColor.systemBlue.withAlphaComponent(0.7).cgColor)
            context.setLineWidth(3)
            if object.kind == .ball || object.kind == .pulley || object.kind == .ring {
                context.strokeEllipse(in: CGRect(x: -object.radius - 5, y: -object.radius - 5, width: object.radius * 2 + 10, height: object.radius * 2 + 10))
            } else {
                context.stroke(CGRect(x: -object.size.width * 0.5 - 5, y: -object.size.height * 0.5 - 5, width: object.size.width + 10, height: object.size.height + 10))
            }
        }
        context.restoreGState()
    }

    private func drawPolygonOrRect(_ object: PhysicsObject, _ context: CGContext) {
        if object.polygon.isEmpty {
            context.beginPath()
            context.addRect(CGRect(x: -object.size.width * 0.5, y: -object.size.height * 0.5, width: object.size.width, height: object.size.height))
            context.drawPath(using: .fillStroke)
        } else {
            context.beginPath()
            context.move(to: object.polygon[0])
            object.polygon.dropFirst().forEach { context.addLine(to: $0) }
            context.closePath()
            context.drawPath(using: .fillStroke)
        }
    }

    private func drawSpring(_ object: PhysicsObject, _ context: CGContext) {
        let left = -object.size.width * 0.5
        let right = object.size.width * 0.5
        let unit = max(1, object.naturalLength / max(0.001, object.realLength) / 6)
        context.move(to: CGPoint(x: left, y: 0))
        var x = left
        while x < right {
            context.addLine(to: CGPoint(x: min(right, x + unit * 0.25), y: -7))
            context.addLine(to: CGPoint(x: min(right, x + unit * 0.75), y: 7))
            context.addLine(to: CGPoint(x: min(right, x + unit), y: 0))
            x += unit
        }
        context.setLineWidth(2.5)
        context.strokePath()
        context.move(to: CGPoint(x: left, y: -10))
        context.addLine(to: CGPoint(x: left, y: 10))
        context.move(to: CGPoint(x: right, y: -10))
        context.addLine(to: CGPoint(x: right, y: 10))
        context.strokePath()
    }

    private func drawArcRamp(_ object: PhysicsObject, _ context: CGContext) {
        context.saveGState()
        defer { context.restoreGState() }
        if !object.horizontal { context.scaleBy(x: -1, y: 1) }
        let width = max(1, object.size.width)
        let height = max(1, object.size.height)
        if width <= 1 {
            let radius = max(20, height * 0.5)
            context.addArc(center: .zero, radius: radius, startAngle: .pi * 0.5, endAngle: .pi * 0.5 - object.arcAngle, clockwise: true)
            context.strokePath()
            if object.pipeMode {
                let innerRadius = max(1, radius - object.pipeDiameter * engine.pixelsPerMeter)
                context.addArc(center: .zero, radius: innerRadius, startAngle: .pi * 0.5, endAngle: .pi * 0.5 - object.arcAngle, clockwise: true)
                context.strokePath()
            }
            return
        }
        let radius = (width * width + height * height) / (2 * height)
        let center = CGPoint(x: -width * 0.5, y: height * 0.5 - radius)
        let endAngle = atan2(-height * 0.5 - center.y, width * 0.5 - center.x)
        context.beginPath()
        context.move(to: CGPoint(x: -width * 0.5, y: height * 0.5))
        if !object.pipeMode {
            context.addLine(to: CGPoint(x: width * 0.5, y: height * 0.5))
            context.addLine(to: CGPoint(x: width * 0.5, y: -height * 0.5))
        }
        context.addArc(center: center, radius: radius, startAngle: endAngle, endAngle: .pi * 0.5, clockwise: false)
        if !object.pipeMode { context.closePath(); context.drawPath(using: .fillStroke) } else { context.strokePath() }
        if object.pipeMode {
            let inner = max(1, radius - object.pipeDiameter * engine.pixelsPerMeter)
            context.addArc(center: center, radius: inner, startAngle: endAngle, endAngle: .pi * 0.5, clockwise: false)
            context.strokePath()
        }
    }

    private func drawContainer(_ object: PhysicsObject, _ context: CGContext) {
        let width = object.size.width * 0.5
        let height = object.size.height * 0.5
        let wall = max(2, object.wallThickness * engine.pixelsPerMeter)
        if object.waterEnabled, object.waterLevel > 0 {
            let waterHeight = object.waterLevel * engine.pixelsPerMeter
            let water = CGRect(x: -width + wall, y: height - wall - waterHeight, width: object.size.width - wall * 2, height: waterHeight)
            context.setFillColor(UIColor(red: 0.23, green: 0.72, blue: 0.94, alpha: 0.9).cgColor)
            context.fill(water)
            context.setStrokeColor(UIColor.white.withAlphaComponent(0.55).cgColor)
            context.setLineWidth(1.5)
            let phase = CGFloat(CACurrentMediaTime().truncatingRemainder(dividingBy: 2.4) / 2.4) * 2 * .pi
            context.move(to: CGPoint(x: water.minX, y: water.minY))
            for index in 0...50 {
                let x = water.minX + water.width * CGFloat(index) / 50
                let y = water.minY + sin(phase + CGFloat(index) / 50 * 4 * .pi) * min(5, water.width * 0.02)
                context.addLine(to: CGPoint(x: x, y: y))
            }
            context.strokePath()
        }
        context.setStrokeColor(UIColor.black.cgColor)
        context.setFillColor(UIColor.white.cgColor)
        context.setLineWidth(2)
        context.beginPath()
        context.move(to: CGPoint(x: -width, y: -height))
        context.addLine(to: CGPoint(x: -width, y: height))
        context.addLine(to: CGPoint(x: width, y: height))
        context.addLine(to: CGPoint(x: width, y: -height))
        context.addLine(to: CGPoint(x: width - wall, y: -height))
        context.addLine(to: CGPoint(x: width - wall, y: height - wall))
        context.addLine(to: CGPoint(x: -width + wall, y: height - wall))
        context.addLine(to: CGPoint(x: -width + wall, y: -height))
        context.closePath()
        context.drawPath(using: .fillStroke)
    }

    private func drawTrajectory(_ object: PhysicsObject, _ context: CGContext) {
        guard object.trajectory.count > 1 else { return }
        context.setStrokeColor(UIColor.systemBlue.cgColor)
        context.setLineWidth(1)
        context.addLines(between: object.trajectory)
        context.strokePath()
    }

    private func drawForces(_ object: PhysicsObject, _ context: CGContext) {
        let pink = UIColor(red: 0.91, green: 0.38, blue: 0.60, alpha: 1)
        let green = UIColor(red: 0.24, green: 0.70, blue: 0.44, alpha: 1)
        var values: [(CGVector, UIColor, String)] = [
            (CGVector(dx: 0, dy: 1), pink, "G")
        ]
        if engine.windForce > 0.001 {
            let radians = -engine.windDirection * .pi / 180
            values.append((CGVector(dx: cos(radians), dy: sin(radians)), green, "Fw"))
        }
        if engine.airResistance > 0.001, object.speed > 0.1 {
            values.append((object.velocity.normalized * -1, green, "Fd"))
        }
        values.append(contentsOf: [
            (object.appliedForce, pink, "F"),
            (object.supportForce, pink, "N"),
            (object.frictionForce, green, "Ff"),
            (object.constraintForce, pink, "Fₜ"),
            (object.reactionForce, green, "N′")
        ])
        for (force, color, label) in values where force.length > 0.001 {
            drawArrow(from: object.position, vector: force.normalized * 42, color: color, label: label, context: context)
        }
    }

    private func drawArrow(from start: CGPoint, vector: CGVector, color: UIColor, label: String, context: CGContext) {
        let end = start + vector
        context.setStrokeColor(color.cgColor)
        context.setFillColor(color.cgColor)
        context.setLineWidth(2.5)
        context.move(to: start)
        context.addLine(to: end)
        let angle = atan2(vector.dy, vector.dx)
        context.addLine(to: CGPoint(x: end.x - 9 * cos(angle - 0.45), y: end.y - 9 * sin(angle - 0.45)))
        context.move(to: end)
        context.addLine(to: CGPoint(x: end.x - 9 * cos(angle + 0.45), y: end.y - 9 * sin(angle + 0.45)))
        context.strokePath()
        label.draw(at: CGPoint(x: end.x + 4, y: end.y - 16), withAttributes: [.font: UIFont.systemFont(ofSize: 12), .foregroundColor: color])
    }

    private func drawState(_ object: PhysicsObject, _ context: CGContext) {
        let displacement = hypot(object.position.x - object.initialPosition.x, object.position.y - object.initialPosition.y) / engine.pixelsPerMeter
        let text = "v=\(format(object.speed))m/s\nx=\(format(displacement))m\na=\(format(object.acceleration.length))m/s²" + (object.pressure > 0 ? "\nP=\(format(object.pressure))Pa" : "")
        let paragraph = NSMutableParagraphStyle()
        paragraph.alignment = .left
        let origin = stateGraphOrigin(object)
        text.draw(in: CGRect(x: origin.x, y: origin.y + 22, width: 190, height: 70), withAttributes: [.font: UIFont.monospacedSystemFont(ofSize: 10, weight: .regular), .foregroundColor: UIColor.black, .paragraphStyle: paragraph])
        drawGraphs(object)
    }

    private func stateGraphOrigin(_ object: PhysicsObject) -> CGPoint {
        let graphX = max(5, object.position.x - 95)
        let above = object.position.y - object.radius - 80
        let graphY = above >= 5 ? above : object.position.y + object.radius + 10
        return CGPoint(x: graphX, y: graphY)
    }

    private func drawGraphs(_ object: PhysicsObject) {
        guard let context = UIGraphicsGetCurrentContext() else { return }
        let series = [object.velocityHistory, object.positionHistory, object.accelerationHistory]
        let colors = [UIColor.red, .green, .blue]
        let limits: [CGFloat] = [5, 5, 20]
        let width: CGFloat = 60
        let height: CGFloat = 15
        let origin = stateGraphOrigin(object)
        for index in series.indices {
            let rect = CGRect(x: origin.x + CGFloat(index) * 65, y: origin.y, width: width, height: height)
            context.setFillColor(UIColor(white: 0.93, alpha: 1).cgColor)
            context.fill(rect)
            let values = series[index]
            guard values.count > 1 else { continue }
            context.setStrokeColor(colors[index].cgColor)
            context.setLineWidth(1)
            for valueIndex in values.indices {
                let normalized = max(-1, min(1, values[valueIndex] / limits[index]))
                let point = CGPoint(x: rect.minX + rect.width * CGFloat(valueIndex) / CGFloat(values.count), y: rect.midY - normalized * rect.height * 0.5)
                if valueIndex == 0 { context.move(to: point) } else { context.addLine(to: point) }
            }
            context.strokePath()
        }
    }

    private func drawPreview(_ context: CGContext) {
        guard let start = previewStart, let end = previewEnd else { return }
        switch mode {
        case .velocity:
            drawArrow(from: start, vector: end - start, color: .systemBlue, label: "v₀", context: context)
        case .force:
            drawArrow(from: start, vector: end - start, color: .systemRed, label: "F", context: context)
        case .draw:
            context.setStrokeColor(UIColor.darkGray.cgColor)
            context.setLineWidth(3)
            context.move(to: start)
            context.addLine(to: end)
            context.strokePath()
            drawMeasurement(start, end)
        case .cut:
            context.setStrokeColor(UIColor.systemRed.cgColor)
            context.setLineDash(phase: 0, lengths: [12, 6])
            context.setLineWidth(2)
            context.move(to: start)
            context.addLine(to: end)
            context.strokePath()
            if currentCutPercentage > 0 {
                String(format: "切割: %.1f%%", Double(currentCutPercentage)).draw(at: CGPoint(x: end.x + 10, y: end.y - 24), withAttributes: [.font: UIFont.systemFont(ofSize: 17, weight: .medium), .foregroundColor: UIColor.systemRed])
            }
        case .erase:
            drawMeasurement(start, end)
        case .normal:
            break
        }
    }

    private func drawMeasurement(_ start: CGPoint, _ end: CGPoint) {
        let distance = hypot(end.x - start.x, end.y - start.y) / engine.pixelsPerMeter
        format(distance).appending("m").draw(at: CGPoint(x: (start.x + end.x) * 0.5, y: (start.y + end.y) * 0.5 - 18), withAttributes: [.font: UIFont.systemFont(ofSize: 12), .foregroundColor: UIColor.black])
        guard let context = UIGraphicsGetCurrentContext() else { return }
        context.setStrokeColor(UIColor.black.cgColor)
        context.setLineWidth(1)
        let left = start.x - engine.wallLeftX
        let right = engine.wallRightX - start.x
        let horizontalEnd = left <= right ? CGPoint(x: engine.wallLeftX, y: start.y) : CGPoint(x: engine.wallRightX, y: start.y)
        context.move(to: start)
        context.addLine(to: horizontalEnd)
        context.strokePath()
        (format(min(left, right) / engine.pixelsPerMeter) + "m").draw(at: CGPoint(x: (start.x + horizontalEnd.x) * 0.5, y: start.y - 16), withAttributes: [.font: UIFont.systemFont(ofSize: 11), .foregroundColor: UIColor.black])
        let ground = engine.groundY - start.y
        let top = start.y
        let verticalEnd = ground <= top ? CGPoint(x: start.x, y: engine.groundY) : CGPoint(x: start.x, y: 0)
        context.move(to: start)
        context.addLine(to: verticalEnd)
        context.strokePath()
        (format(min(ground, top) / engine.pixelsPerMeter) + "m").draw(at: CGPoint(x: start.x + 4, y: (start.y + verticalEnd.y) * 0.5), withAttributes: [.font: UIFont.systemFont(ofSize: 11), .foregroundColor: UIColor.black])
    }

    private func drawDragMeasurements(_ object: PhysicsObject, context: CGContext) {
        let bounds = object.bounds
        var nearestVertical = CGFloat.greatestFiniteMagnitude
        var vertical: (CGPoint, CGPoint, String)?
        var nearestHorizontal = CGFloat.greatestFiniteMagnitude
        var horizontal: (CGPoint, CGPoint, String)?
        let groundDistance = (engine.groundY - bounds.maxY) / engine.pixelsPerMeter
        if groundDistance >= 0, groundDistance < 5 { nearestVertical = groundDistance; vertical = (CGPoint(x: object.position.x, y: bounds.maxY), CGPoint(x: object.position.x, y: engine.groundY), format(groundDistance) + "m") }
        let leftDistance = (bounds.minX - engine.wallLeftX) / engine.pixelsPerMeter
        if leftDistance >= 0, leftDistance < 5 { nearestHorizontal = leftDistance; horizontal = (CGPoint(x: engine.wallLeftX, y: object.position.y), CGPoint(x: bounds.minX, y: object.position.y), format(leftDistance) + "m") }
        let rightDistance = (engine.wallRightX - bounds.maxX) / engine.pixelsPerMeter
        if rightDistance >= 0, rightDistance < 5, rightDistance < nearestHorizontal { nearestHorizontal = rightDistance; horizontal = (CGPoint(x: bounds.maxX, y: object.position.y), CGPoint(x: engine.wallRightX, y: object.position.y), format(rightDistance) + "m") }
        if let ramp = object.rampContact {
            let angle = ramp.kind == .straightRamp ? atan2(ramp.horizontal ? -ramp.size.height : ramp.size.height, ramp.size.width) : object.rotation
            let tangent = CGVector(dx: cos(angle), dy: sin(angle))
            let normal = CGVector(dx: -sin(angle), dy: cos(angle))
            let objectTangent = object.position.x * tangent.dx + object.position.y * tangent.dy
            let objectNormal = object.position.x * normal.dx + object.position.y * normal.dy
            for other in engine.objects where other !== object && !engine.isConstraintBody(other) {
                let otherTangent = other.position.x * tangent.dx + other.position.y * tangent.dy
                let otherNormal = other.position.x * normal.dx + other.position.y * normal.dy
                let tangentDistance = abs(otherTangent - objectTangent) / engine.pixelsPerMeter
                if tangentDistance < 3, tangentDistance < nearestHorizontal {
                    nearestHorizontal = tangentDistance
                    let sign: CGFloat = otherTangent > objectTangent ? 1 : -1
                    horizontal = (object.position, object.position + tangent * (sign * tangentDistance * engine.pixelsPerMeter), format(tangentDistance) + "m")
                }
                let normalDistance = abs(otherNormal - objectNormal) / engine.pixelsPerMeter
                if normalDistance < 3, normalDistance < nearestVertical {
                    nearestVertical = normalDistance
                    let sign: CGFloat = otherNormal > objectNormal ? 1 : -1
                    vertical = (object.position, object.position + normal * (sign * normalDistance * engine.pixelsPerMeter), format(normalDistance) + "m")
                }
            }
        } else {
            for other in engine.objects where other !== object && !engine.isConstraintBody(other) {
                let otherBounds = other.bounds
                let verticalGap = (otherBounds.minY - bounds.maxY) / engine.pixelsPerMeter
                let overlapX = min(bounds.maxX, otherBounds.maxX) - max(bounds.minX, otherBounds.minX)
                if verticalGap > 0, verticalGap < 3, verticalGap < nearestVertical, overlapX > 0 {
                    nearestVertical = verticalGap
                    let x = (max(bounds.minX, otherBounds.minX) + min(bounds.maxX, otherBounds.maxX)) * 0.5
                    vertical = (CGPoint(x: x, y: bounds.maxY), CGPoint(x: x, y: otherBounds.minY), format(verticalGap) + "m")
                }
                let rightGap = (otherBounds.minX - bounds.maxX) / engine.pixelsPerMeter
                let overlapY = min(bounds.maxY, otherBounds.maxY) - max(bounds.minY, otherBounds.minY)
                if rightGap > 0, rightGap < 3, rightGap < nearestHorizontal, overlapY > 0 {
                    nearestHorizontal = rightGap
                    let y = (max(bounds.minY, otherBounds.minY) + min(bounds.maxY, otherBounds.maxY)) * 0.5
                    horizontal = (CGPoint(x: bounds.maxX, y: y), CGPoint(x: otherBounds.minX, y: y), format(rightGap) + "m")
                }
                let leftGap = (bounds.minX - otherBounds.maxX) / engine.pixelsPerMeter
                if leftGap > 0, leftGap < 3, leftGap < nearestHorizontal, overlapY > 0 {
                    nearestHorizontal = leftGap
                    let y = (max(bounds.minY, otherBounds.minY) + min(bounds.maxY, otherBounds.maxY)) * 0.5
                    horizontal = (CGPoint(x: otherBounds.maxX, y: y), CGPoint(x: bounds.minX, y: y), format(leftGap) + "m")
                }
            }
        }
        context.setStrokeColor(UIColor.systemBlue.withAlphaComponent(0.8).cgColor)
        context.setLineWidth(1)
        context.setLineDash(phase: 0, lengths: [4, 3])
        for value in [vertical, horizontal].compactMap({ $0 }) {
            context.move(to: value.0)
            context.addLine(to: value.1)
            context.strokePath()
            value.2.draw(at: CGPoint(x: (value.0.x + value.1.x) * 0.5 + 3, y: (value.0.y + value.1.y) * 0.5 - 15), withAttributes: [.font: UIFont.systemFont(ofSize: 10), .foregroundColor: UIColor.systemBlue])
        }
        context.setLineDash(phase: 0, lengths: [])
    }

    private func format(_ value: CGFloat) -> String { SurdFormatter.text(value, places: engine.decimalPlaces) }
}
