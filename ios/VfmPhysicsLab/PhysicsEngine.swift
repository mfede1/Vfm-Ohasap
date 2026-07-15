import UIKit

final class PhysicsEngine {
    static let fixedTimeStep: CGFloat = 1 / 60
    let gravity: CGFloat = 9.8
    var objects: [PhysicsObject] = []
    var ceilings: [CeilingSegment] = []
    var groundEraseRanges: [ClosedRange<CGFloat>] = []
    var groundY: CGFloat = 600
    var wallLeftX: CGFloat = 0
    var wallRightX: CGFloat = 1000
    var groundLength: CGFloat = 10
    var pixelsPerMeter: CGFloat = 100
    var windForce: CGFloat = 0
    var windDirection: CGFloat = 0
    var airResistance: CGFloat = 0
    var decimalPlaces = 2
    var elapsedTime: CGFloat = 0
    var history: [SimulationState] = []
    var lastHistoryTime: CGFloat = -1

    func updateWorld(size: CGSize) {
        groundY = size.height * 0.85
        wallLeftX = 0
        wallRightX = size.width
        pixelsPerMeter = max(1, size.width / groundLength)
    }

    func updateObjectSizes() {
        for object in objects { updateObjectSize(object) }
    }

    func updateObjectSize(_ object: PhysicsObject) {
            switch object.kind {
            case .block, .board, .straightRamp, .container:
                object.size = CGSize(width: object.realWidth * pixelsPerMeter, height: object.realHeight * pixelsPerMeter)
            case .arcRamp:
                object.size = CGSize(width: object.realWidth * pixelsPerMeter, height: object.realHeight * pixelsPerMeter)
            case .ball, .pulley, .ring:
                object.size = CGSize(width: object.realRadius * 2 * pixelsPerMeter, height: object.realRadius * 2 * pixelsPerMeter)
            case .spring:
                let length = object.realLength * pixelsPerMeter
                object.size = CGSize(width: length, height: 20)
                if !object.horizontal, abs(object.rotation) < 0.001 { object.rotation = .pi * 0.5 }
                object.naturalLength = length
            case .rope:
                let length = object.realLength * pixelsPerMeter
                object.size = CGSize(width: length, height: 10)
                if !object.horizontal, abs(object.rotation) < 0.001 { object.rotation = .pi * 0.5 }
                object.ropeLength = length
            case .rod:
                object.rodLength = object.realLength * pixelsPerMeter
                object.size = CGSize(width: object.rodLength, height: max(6, object.realHeight * pixelsPerMeter))
                if !object.horizontal, abs(object.rotation) < 0.001 { object.rotation = .pi * 0.5 }
            }
    }

    func createObject(_ kind: ObjectKind, at position: CGPoint) -> PhysicsObject {
        let scale = groundLength / 10
        let object: PhysicsObject
        switch kind {
        case .block:
            object = PhysicsObject(kind: kind, position: position, size: CGSize(width: 0.6 * scale * pixelsPerMeter, height: 0.4 * scale * pixelsPerMeter))
            object.realWidth = 0.6 * scale
            object.realHeight = 0.4 * scale
        case .board:
            object = PhysicsObject(kind: kind, position: position, size: CGSize(width: 1.6 * scale * pixelsPerMeter, height: 0.12 * scale * pixelsPerMeter))
            object.realWidth = 1.6 * scale
            object.realHeight = 0.12 * scale
        case .spring:
            let length = 0.8 * scale * pixelsPerMeter
            object = PhysicsObject(kind: kind, position: position, size: CGSize(width: length, height: 20))
            object.realLength = 0.8 * scale
            object.naturalLength = length
            object.elasticLimit = 500
            object.freeEnd = CGPoint(x: position.x - length * 0.5, y: position.y)
        case .rope:
            let length = scale * pixelsPerMeter
            object = PhysicsObject(kind: kind, position: position, size: CGSize(width: length, height: 10))
            object.realLength = scale
            object.ropeLength = length
            object.springConstant = 1000
            object.elasticLimit = 1000
            object.freeEnd = CGPoint(x: position.x - length * 0.5, y: position.y)
        case .straightRamp:
            object = PhysicsObject(kind: kind, position: position, size: CGSize(width: 1.8 * scale * pixelsPerMeter, height: scale * pixelsPerMeter))
            object.realWidth = 1.8 * scale
            object.realHeight = scale
        case .arcRamp:
            object = PhysicsObject(kind: kind, position: position, size: CGSize(width: 1.4 * scale * pixelsPerMeter, height: scale * pixelsPerMeter))
            object.realWidth = 1.4 * scale
            object.realHeight = scale
        case .ball:
            let radius = 0.2 * scale
            object = PhysicsObject(kind: kind, position: position, size: CGSize(width: radius * 2 * pixelsPerMeter, height: radius * 2 * pixelsPerMeter))
            object.realRadius = radius
        case .container:
            object = PhysicsObject(kind: kind, position: position, size: CGSize(width: 1.5 * scale * pixelsPerMeter, height: scale * pixelsPerMeter))
            object.mass = 2
            object.restitution = 0.1
            object.realWidth = 1.5 * scale
            object.realHeight = scale
            object.waterEnabled = true
            object.waterLevel = (object.realHeight - object.wallThickness) * 0.5
        case .pulley:
            let radius = 0.18 * scale
            object = PhysicsObject(kind: kind, position: position, size: CGSize(width: radius * 2 * pixelsPerMeter, height: radius * 2 * pixelsPerMeter))
            object.mass = 0.5
            object.restitution = 0.1
            object.realRadius = radius
        case .rod:
            let length = 0.8 * scale * pixelsPerMeter
            object = PhysicsObject(kind: kind, position: position, size: CGSize(width: length, height: 6))
            object.realLength = 0.8 * scale
            object.realHeight = 0
            object.rodLength = length
        case .ring:
            let radius: CGFloat = 0.12
            object = PhysicsObject(kind: kind, position: position, size: CGSize(width: radius * 2 * pixelsPerMeter, height: radius * 2 * pixelsPerMeter))
            object.mass = 0.2
            object.realRadius = radius
        }
        object.initialPosition = position
        object.initialVelocity = .zero
        return object
    }

    func add(_ kind: ObjectKind, at position: CGPoint) -> PhysicsObject {
        let object = createObject(kind, at: position)
        objects.append(object)
        return object
    }

    func remove(_ object: PhysicsObject) {
        let removed = objects.filter { $0 === object || $0.ringHostRod === object }
        let removedIDs = Set(removed.map(\.id))
        for item in objects where !removedIDs.contains(item.id) {
            if item.rodRing === object { item.rodRing = nil }
            if case .object(let id) = item.anchorA, removedIDs.contains(id) { setAnchor(.none, on: item, endA: true) }
            if case .object(let id) = item.anchorB, removedIDs.contains(id) { setAnchor(.none, on: item, endA: false) }
        }
        objects.removeAll { removedIDs.contains($0.id) }
    }

    func reset(clear: Bool) {
        if clear {
            objects.removeAll()
            ceilings.removeAll()
            groundEraseRanges.removeAll()
            history.removeAll()
        } else {
            restoreInitialState()
            history.removeAll()
        }
        elapsedTime = 0
        lastHistoryTime = -1
        objects.forEach {
            $0.trajectory.removeAll()
            $0.velocityHistory.removeAll()
            $0.positionHistory.removeAll()
            $0.accelerationHistory.removeAll()
            [$0.forceCondition, $0.velocityCondition, $0.bindACondition, $0.bindBCondition, $0.wrapCondition, $0.fixedCondition, $0.angleFixedCondition].forEach { $0?.reset() }
        }
        saveState()
    }

    func saveInitialState() {
        for object in objects {
            object.initialPosition = object.position
            object.initialVelocity = object.velocity
        }
        history.removeAll()
        elapsedTime = 0
        saveState()
    }

    func savePlaybackBaseline() {
        for object in objects {
            object.initialPosition = object.position
            object.initialVelocity = object.velocity
        }
        history.removeAll()
        lastHistoryTime = -1
        saveState()
    }

    func restoreInitialState() {
        let initial = history.min(by: { $0.time < $1.time })
        for object in objects {
            if let value = initial?.states[object.id] {
                object.position = value.position
                object.velocity = value.velocity
                object.rotation = value.rotation
                object.freeEnd = value.freeEnd
                object.freeEndVelocity = value.freeEndVelocity
            } else {
                object.position = object.initialPosition
                object.velocity = object.initialVelocity
                object.rotation = 0
                object.freeEndVelocity = .zero
            }
            object.acceleration = .zero
            object.force = .zero
            object.displayForce = .zero
            object.constraintForce = .zero
            object.supportForce = .zero
            object.frictionForce = .zero
            object.reactionForce = .zero
            object.forceTimer = 0
            object.contactGround = false
            object.contactLeftWall = false
            object.contactRightWall = false
            object.contactRamp = false
            object.rampContact = nil
            object.circleMomentumInitialized = false
            object.conserveCircleHorizontal = false
            object.circleSystemMemberIDs.removeAll()
        }
    }

    func saveState() {
        let states = Dictionary(uniqueKeysWithValues: objects.map {
            ($0.id, ObjectState(position: $0.position, velocity: $0.velocity, rotation: $0.rotation, freeEnd: $0.freeEnd, freeEndVelocity: $0.freeEndVelocity))
        })
        history.append(SimulationState(time: elapsedTime, states: states))
        if history.count > 620 { history.removeFirst(history.count - 620) }
        lastHistoryTime = elapsedTime
    }

    func restore(at time: CGFloat) {
        guard !history.isEmpty else {
            restoreInitialState()
            elapsedTime = 0
            simulate(to: time)
            return
        }
        let exact = history.last(where: { abs($0.time - time) < 0.0005 })
        let prior = history.filter { $0.time <= time + 0.0005 }.max(by: { $0.time < $1.time })
        let earliest = history.min(by: { $0.time < $1.time })!
        let state = exact ?? prior ?? earliest
        for object in objects {
            guard let value = state.states[object.id] else { continue }
            object.position = value.position
            object.velocity = value.velocity
            object.rotation = value.rotation
            object.freeEnd = value.freeEnd
            object.freeEndVelocity = value.freeEndVelocity
            object.acceleration = .zero
            object.force = .zero
            object.displayForce = .zero
            object.constraintForce = .zero
            object.supportForce = .zero
            object.frictionForce = .zero
            object.reactionForce = .zero
            object.forceTimer = 0
            object.contactGround = false
            object.contactLeftWall = false
            object.contactRightWall = false
            object.contactRamp = false
            object.rampContact = nil
            object.circleMomentumInitialized = false
            object.conserveCircleHorizontal = false
            object.circleSystemMemberIDs.removeAll()
        }
        elapsedTime = state.time
        if time > elapsedTime + 0.0005 { simulate(to: time) }
    }

    func simulate(to target: CGFloat) {
        if target < elapsedTime { restoreInitialState(); elapsedTime = 0 }
        while elapsedTime < target - 0.0005 {
            let timeStep = min(Self.fixedTimeStep, target - elapsedTime)
            step(timeStep)
            elapsedTime += timeStep
            if elapsedTime - lastHistoryTime >= 0.1 || target - elapsedTime <= 0.0005 { saveState() }
        }
    }

    func saveStateIfNeeded() {
        if elapsedTime - lastHistoryTime >= 0.1 { saveState() }
    }

    func step(_ timeStep: CGFloat) {
        guard timeStep > 0, timeStep.isFinite else { return }
        let startingVelocities = Dictionary(uniqueKeysWithValues: objects.map { ($0.id, $0.velocity) })
        var maximumRatio: CGFloat = 0
        for object in objects where !object.fixed && !isConstraintBody(object) {
            let minimumSize = max(6, min(max(1, object.size.width), max(1, object.size.height)))
            maximumRatio = max(maximumRatio, object.speed * pixelsPerMeter * timeStep / minimumSize)
        }
        var substeps = max(1, Int(ceil(maximumRatio / 0.35)))
        if timeStep > Self.fixedTimeStep { substeps = max(substeps, Int(ceil(timeStep / Self.fixedTimeStep))) }
        substeps = min(12, substeps)
        let dt = timeStep / CGFloat(substeps)
        for _ in 0..<substeps { stepInternal(dt) }
        recordMotion(timeStep, startingVelocities: startingVelocities)
    }

    private func stepInternal(_ dt: CGFloat) {
        applyConditions(dt)
        for object in objects {
            object.previousPosition = object.position
            object.previousVelocity = object.velocity
            object.previousContactRamp = object.contactRamp
            object.stepMechanicalEnergy = 0.5 * object.mass * object.speed * object.speed - object.mass * gravity * object.position.y / pixelsPerMeter
            object.contactGround = false
            object.contactLeftWall = false
            object.contactRightWall = false
            object.contactRamp = false
            object.rampContact = nil
            object.supportForce = .zero
            object.frictionForce = .zero
            object.reactionForce = .zero
            if object.fixed || isConstraintBody(object) { object.velocity = .zero; continue }
            let windRadians = -windDirection * .pi / 180
            var force = CGVector(dx: windForce * cos(windRadians), dy: object.mass * gravity + windForce * sin(windRadians))
            if let condition = object.forceCondition, evaluate(condition, owner: object, dt: dt) { object.appliedForce = .zero; object.forceCondition = nil }
            if let condition = object.velocityCondition, evaluate(condition, owner: object, dt: dt) { object.velocity = .zero; object.velocityCondition = nil }
            force = force + object.appliedForce
            if airResistance > 0, object.speed > 0.0001 {
                force = force - object.velocity.normalized * airResistance
            }
            object.force = force
            object.displayForce = force
            object.acceleration = force / max(0.001, object.mass)
            object.position = object.position + object.velocity * (dt * pixelsPerMeter) + object.acceleration * (0.5 * dt * dt * pixelsPerMeter)
            object.velocity = object.velocity + object.acceleration * dt
            if object.speed > 50 { object.velocity = object.velocity.normalized * 50 }
            if object.forceDuration >= 0 {
                object.forceTimer += dt
                if object.forceTimer >= object.forceDuration { object.appliedForce = .zero; object.forceDuration = -1; object.forceTimer = 0 }
            }
        }
        prepareCircularHorizontalConservation()
        updateBoundPulleys()
        solveLinks(dt)
        solveWrappedRopes(dt)
        solveRings(dt)
        solveWorldCollisions(dt)
        solvePairCollisions(dt)
        solveSpringBodyCollisions(dt)
        solveRampCollisions(dt)
        solveContainers(dt)
        alignObjectsOnRotatedSurfaces()
        resetUnsupportedRotations()
        enforceCircularHorizontalConservation(dt)
        updateCutHalves(dt)
    }

    private func applyConditions(_ dt: CGFloat) {
        for object in objects {
            if let condition = object.fixedCondition, evaluate(condition, owner: object, dt: dt) { if object.kind == .rod { object.remoteLock = false } else { object.fixed = false }; object.fixedCondition = nil }
            if let condition = object.angleFixedCondition, evaluate(condition, owner: object, dt: dt) { object.angleFixed = false; object.angleFixedCondition = nil }
            if let condition = object.bindACondition, evaluate(condition, owner: object, dt: dt) { if object.kind == .pulley { object.pulleyRamp = nil } else { setAnchor(.none, on: object, endA: true) }; object.bindACondition = nil }
            if let condition = object.bindBCondition, evaluate(condition, owner: object, dt: dt) { setAnchor(.none, on: object, endA: false); object.bindBCondition = nil }
            if let condition = object.wrapCondition, evaluate(condition, owner: object, dt: dt) { object.ropeWrapped = false; object.wrapPoints.removeAll(); object.ropeParticles.removeAll(); object.ropeParticlesInitialized = false; object.wrapCondition = nil }
        }
    }

    private func evaluate(_ condition: LogicCondition?, owner: PhysicsObject, dt: CGFloat) -> Bool {
        guard let condition else { return true }
        if condition.triggered { return true }
        let target = condition.targetID.flatMap { id in objects.first { $0.id == id } }
        switch condition.kind {
        case .afterTime:
            if condition.startTime < 0 { condition.startTime = elapsedTime }
            condition.triggered = elapsedTime - condition.startTime >= condition.time
        case .contactObject:
            condition.triggered = target.map { isInContact(owner, $0) } ?? false
        case .timerReach:
            condition.triggered = elapsedTime >= condition.time
        case .objectMissing:
            condition.triggered = target == nil
        case .contactDuration:
            if let target, isInContact(owner, target) {
                if condition.startTime < 0 { condition.startTime = elapsedTime }
                condition.triggered = elapsedTime - condition.startTime >= condition.time
            } else { condition.startTime = -1 }
        case .contactTotal:
            if let target, isInContact(owner, target) { condition.totalTimer += dt }
            condition.triggered = condition.totalTimer >= condition.time
        case .afterNthContact:
            let touching = target.map { isInContact(owner, $0) } ?? false
            if touching && !condition.contactStarted { condition.contactStarted = true; condition.contactCount += 1 }
            if !touching && condition.contactStarted {
                condition.contactStarted = false
                if condition.contactNth == -1 || condition.contactCount == condition.contactNth { condition.startTime = elapsedTime }
            }
            condition.triggered = condition.startTime >= 0 && elapsedTime - condition.startTime >= condition.time
        case .compound:
            let required = condition.requiredCount > 0 ? condition.requiredCount : condition.subconditions.count
            condition.triggered = condition.subconditions.filter { evaluate($0, owner: owner, dt: dt) }.count >= required
        }
        return condition.triggered
    }

    func endpoints(of object: PhysicsObject) -> (CGPoint, CGPoint) {
        if object.isCutHalf { return (object.cutHalfAnchor, object.cutHalfFree) }
        if object.ropeWrapped, object.wrapPoints.count >= 2 { return (object.wrapPoints[0], object.wrapPoints[object.wrapPoints.count - 1]) }
        let length = object.kind == .rod ? object.rodLength : object.size.width
        var direction = CGVector(dx: object.horizontal ? 1 : 0, dy: object.horizontal ? 0 : 1)
        if object.rotation != 0 { direction = CGVector(dx: cos(object.rotation), dy: sin(object.rotation)) }
        let half = direction * (length * 0.5)
        var a = object.position + half * -1
        var b = object.position + half
        if let anchored = point(for: object.anchorA) { a = anchored }
        if let anchored = point(for: object.anchorB) { b = anchored }
        if case .object = object.anchorA, let surface = surfacePoint(for: object.anchorA, toward: b) { a = surface }
        if case .object = object.anchorB, let surface = surfacePoint(for: object.anchorB, toward: a) { b = surface }
        return (a, b)
    }

    func setAnchor(_ anchor: Anchor, on object: PhysicsObject, endA: Bool) {
        let ends = endpoints(of: object)
        if anchor == .none {
            object.freeEnd = endA ? ends.0 : ends.1
        } else if endA, object.anchorB == .none {
            object.freeEnd = ends.1
        } else if !endA, object.anchorA == .none {
            object.freeEnd = ends.0
        }
        if endA { object.anchorA = anchor } else { object.anchorB = anchor }
        object.freeEndVelocity = .zero
    }

    func point(for anchor: Anchor) -> CGPoint? {
        switch anchor {
        case .none: return nil
        case .object(let id): return objects.first { $0.id == id }?.position
        case .ceiling(let index, let x):
            guard ceilings.indices.contains(index) else { return nil }
            let segment = ceilings[index]
            let t = segment.end.x == segment.start.x ? 0 : (x - segment.start.x) / (segment.end.x - segment.start.x)
            return CGPoint(x: x, y: segment.start.y + (segment.end.y - segment.start.y) * t)
        case .ground(let x): return CGPoint(x: x, y: groundY)
        case .leftWall(let y): return CGPoint(x: wallLeftX, y: y)
        case .rightWall(let y): return CGPoint(x: wallRightX, y: y)
        }
    }

    func isConstraintBody(_ object: PhysicsObject) -> Bool { object.kind == .spring || object.kind == .rope || object.kind == .rod }

    func isFreeEndConstraint(_ object: PhysicsObject) -> Bool {
        guard object.kind == .spring || object.kind == .rope else { return false }
        let anchors = [object.anchorA, object.anchorB]
        let objectCount = anchors.filter { if case .object = $0 { return true }; return false }.count
        let fixedCount = anchors.filter { if case .ceiling = $0 { return true }; if case .ground = $0 { return true }; if case .leftWall = $0 { return true }; if case .rightWall = $0 { return true }; return false }.count
        return objectCount == 1 && fixedCount == 0
    }

    func ceilingIndex(at point: CGPoint) -> Int? {
        ceilings.indices.reversed().first { distanceToSegment(point, ceilings[$0].start, ceilings[$0].end) <= 18 }
    }

    func refreshConstraintPose(_ object: PhysicsObject) {
        guard isConstraintBody(object), !object.isCutHalf else { return }
        solveLinks(Self.fixedTimeStep)
    }

    private func solveLinks(_ dt: CGFloat) {
        var broken: [PhysicsObject] = []
        for object in objects where object.kind == .spring || object.kind == .rope || object.kind == .rod {
            if object.isCutHalf { continue }
            if object.kind == .rope, object.ropeWrapped { continue }
            if object.kind == .rope { autoWrapRope(object) }
            if object.ropeWrapped { continue }
            let anchors = [object.anchorA, object.anchorB]
            let objectAnchors = anchors.compactMap { anchor -> PhysicsObject? in
                guard case .object(let id) = anchor else { return nil }
                return objects.first { $0.id == id }
            }
            let fixedAnchorCount = anchors.filter { if case .ceiling = $0 { return true }; if case .ground = $0 { return true }; if case .leftWall = $0 { return true }; if case .rightWall = $0 { return true }; return false }.count
            if objectAnchors.count == 1, fixedAnchorCount == 0 {
                solveFreeEndConstraint(object, attached: objectAnchors[0], dt: dt)
                continue
            }
            if objectAnchors.isEmpty, fixedAnchorCount == 1 {
                if solveSingleFixedConstraint(object, dt: dt) { broken.append(object) }
                continue
            }
            if objectAnchors.isEmpty, fixedAnchorCount == 0 { continue }
            var (a, b) = endpoints(of: object)
            if object.angleFixed {
                var axis = CGVector(dx: cos(object.fixedAngle), dy: sin(object.fixedAngle))
                var span = (b - a).dot(axis)
                if span < 0 { axis = axis * -1; span = -span }
                if span < 0.001 { span = (b - a).length }
                let targetB = a + axis * span
                if moveAnchor(object.anchorB, by: targetB - b, along: axis, lock: object.kind == .rod && object.remoteLock) { b = targetB }
                else {
                    let targetA = b + axis * -span
                    if moveAnchor(object.anchorA, by: targetA - a, along: axis, lock: object.kind == .rod && object.remoteLock) { a = targetA }
                    else { b = targetB }
                }
            }
            var delta = b - a
            var distance = max(0.001, delta.length)
            var direction = delta / distance
            let target = object.kind == .spring ? object.naturalLength : (object.kind == .rod ? object.rodLength : object.ropeLength)
            if object.kind == .rod, object.remoteLock {
                let targetB = a + direction * target
                if moveAnchor(object.anchorB, by: targetB - b, along: direction, lock: object.angleFixed) { b = targetB }
                else {
                    let targetA = b + direction * -target
                    if moveAnchor(object.anchorA, by: targetA - a, along: direction, lock: object.angleFixed) { a = targetA }
                    else { b = targetB }
                }
                delta = b - a
                distance = max(0.001, delta.length)
                direction = delta / distance
            }
            let stretch = distance - target
            object.deformation = stretch / pixelsPerMeter
            let maximumStretch = object.springConstant > 0 ? object.elasticLimit / object.springConstant * pixelsPerMeter : .greatestFiniteMagnitude
            if object.kind == .spring, stretch > maximumStretch { broken.append(object); continue }
            if object.kind == .rope, stretch > maximumStretch { broken.append(object); continue }
            var magnitude: CGFloat
            if object.kind == .spring {
                let rawForce = object.springConstant * stretch * 0.1
                let limitedForce = max(-5000, min(5000, rawForce))
                magnitude = limitedForce / pixelsPerMeter
            } else if object.kind == .rope {
                let rawForce = max(0, object.springConstant * stretch * 0.5)
                magnitude = min(object.elasticLimit, rawForce) / pixelsPerMeter
            } else { magnitude = 0 }
            let force = direction * magnitude
            if object.kind == .rope, stretch > 0 { applyRopeSeparationImpulse(object, direction: direction, tension: magnitude, dt: dt) }
            applyLinkForce(force, to: object.anchorA, dt: dt)
            applyLinkForce(force * -1, to: object.anchorB, dt: dt)
            object.constraintForce = force
            if object.kind == .rod {
                enforceRodLength(object, direction: direction, distance: distance, target: target, dt: dt)
                let updated = endpoints(of: object)
                a = updated.0
                b = updated.1
                delta = b - a
                distance = max(0.001, delta.length)
            } else if object.fixed && distance > target {
                enforceMaximumLength(object, direction: direction, distance: distance, target: target)
                let updated = endpoints(of: object)
                a = updated.0
                b = updated.1
                delta = b - a
                distance = max(0.001, delta.length)
            }
            object.position = CGPoint(x: (a.x + b.x) * 0.5, y: (a.y + b.y) * 0.5)
            object.rotation = atan2(delta.dy, delta.dx)
            object.horizontal = true
            object.size.width = distance
            if object.angleFixed { object.rotation = object.fixedAngle }
            if object.kind == .rope, object.ropeWrapped { updateWrapPath(object) }
        }
        broken.forEach(remove)
    }

    private func moveAnchor(_ anchor: Anchor, by delta: CGVector, along axis: CGVector, lock: Bool) -> Bool {
        guard case .object(let id) = anchor, let attached = objects.first(where: { $0.id == id }), !attached.fixed else { return false }
        attached.position = attached.position + delta
        if lock { attached.velocity = .zero } else { attached.velocity = axis * attached.velocity.dot(axis) }
        return true
    }

    private func enforceRodLength(_ rod: PhysicsObject, direction: CGVector, distance: CGFloat, target: CGFloat, dt: CGFloat) {
        let tolerance = max(1.5, pixelsPerMeter * 0.01)
        let first: PhysicsObject?
        let second: PhysicsObject?
        if case .object(let id) = rod.anchorA { first = objects.first { $0.id == id && !$0.fixed } } else { first = nil }
        if case .object(let id) = rod.anchorB { second = objects.first { $0.id == id && !$0.fixed } } else { second = nil }
        let inverseFirst: CGFloat = first.map { 1 / max(0.001, $0.mass) } ?? 0
        let inverseSecond: CGFloat = second.map { 1 / max(0.001, $0.mass) } ?? 0
        let inverseTotal = inverseFirst + inverseSecond
        guard inverseTotal > 0 else { return }
        let error = distance - target
        if abs(error) > tolerance {
            if let first { first.position = first.position + direction * (error * inverseFirst / inverseTotal) }
            if let second { second.position = second.position - direction * (error * inverseSecond / inverseTotal) }
        }
        let firstVelocity = first?.velocity ?? .zero
        let secondVelocity = second?.velocity ?? .zero
        let relative = (secondVelocity - firstVelocity).dot(direction)
        let impulse = relative / inverseTotal
        if let first { first.velocity = first.velocity + direction * (impulse * inverseFirst) }
        if let second { second.velocity = second.velocity - direction * (impulse * inverseSecond) }
        let constraint = direction * (impulse / max(0.0001, dt))
        if let first { first.constraintForce = first.constraintForce + constraint }
        if let second { second.constraintForce = second.constraintForce - constraint }
        if rod.angleFixed, let first, second == nil { first.constraintForce = first.constraintForce - first.displayForce }
        if rod.angleFixed, let second, first == nil { second.constraintForce = second.constraintForce - second.displayForce }
    }

    private func enforceMaximumLength(_ link: PhysicsObject, direction: CGVector, distance: CGFloat, target: CGFloat) {
        let first: PhysicsObject?
        let second: PhysicsObject?
        if case .object(let id) = link.anchorA { first = objects.first { $0.id == id && !$0.fixed } } else { first = nil }
        if case .object(let id) = link.anchorB { second = objects.first { $0.id == id && !$0.fixed } } else { second = nil }
        let inverseFirst: CGFloat = first.map { 1 / max(0.001, $0.mass) } ?? 0
        let inverseSecond: CGFloat = second.map { 1 / max(0.001, $0.mass) } ?? 0
        let inverseTotal = inverseFirst + inverseSecond
        guard inverseTotal > 0, distance > target else { return }
        let error = distance - target
        if let first { first.position = first.position + direction * (error * inverseFirst / inverseTotal) }
        if let second { second.position = second.position - direction * (error * inverseSecond / inverseTotal) }
        let relative = ((second?.velocity ?? .zero) - (first?.velocity ?? .zero)).dot(direction)
        guard relative > 0 else { return }
        let impulse = relative / inverseTotal
        if let first { first.velocity = first.velocity + direction * (impulse * inverseFirst) }
        if let second { second.velocity = second.velocity - direction * (impulse * inverseSecond) }
    }

    private func solveFreeEndConstraint(_ link: PhysicsObject, attached: PhysicsObject, dt: CGFloat) {
        let restLength = link.kind == .spring ? link.naturalLength : (link.kind == .rod ? link.rodLength : link.ropeLength)
        let attachedIsA: Bool
        if case .object = link.anchorA { attachedIsA = true } else { attachedIsA = false }
        if (link.freeEnd - attached.position).length < 0.001 {
            let axis = CGVector(dx: cos(link.rotation), dy: sin(link.rotation))
            let direction = attachedIsA ? axis : axis * -1
            link.freeEnd = attached.position + direction * restLength
        }
        var delta = link.freeEnd - attached.position
        var distance = max(0.001, delta.length)
        var direction = delta / distance
        if link.angleFixed {
            let axis = CGVector(dx: cos(link.fixedAngle), dy: sin(link.fixedAngle))
            direction = attachedIsA ? axis : axis * -1
            let along = max(0.001, delta.dot(direction))
            distance = max(0.001, along)
            link.freeEnd = attached.position + direction * distance
            delta = link.freeEnd - attached.position
        }
        if link.kind == .rod {
            link.freeEnd = attached.position + direction * restLength
            link.freeEndVelocity = .zero
            let finalDelta = link.freeEnd - attached.position
            let orientedDelta = attachedIsA ? finalDelta : finalDelta * -1
            link.position = CGPoint(x: (attached.position.x + link.freeEnd.x) * 0.5, y: (attached.position.y + link.freeEnd.y) * 0.5)
            link.rotation = link.angleFixed ? link.fixedAngle : atan2(orientedDelta.dy, orientedDelta.dx)
            link.horizontal = true
            link.size = CGSize(width: restLength, height: max(6, link.realHeight * pixelsPerMeter))
            link.deformation = 0
            return
        }
        if link.fixed {
            if distance > restLength { link.freeEnd = attached.position + direction * restLength }
            link.freeEndVelocity = .zero
            let finalDelta = link.freeEnd - attached.position
            let orientedDelta = attachedIsA ? finalDelta : finalDelta * -1
            link.position = CGPoint(x: (attached.position.x + link.freeEnd.x) * 0.5, y: (attached.position.y + link.freeEnd.y) * 0.5)
            link.rotation = link.angleFixed ? link.fixedAngle : atan2(orientedDelta.dy, orientedDelta.dx)
            link.horizontal = true
            link.size = CGSize(width: max(1, finalDelta.length), height: link.kind == .spring ? 20 : 10)
            link.deformation = 0
            return
        }
        let freeMass: CGFloat = 0.05
        let stretch = distance - restLength
        var springForceMagnitude: CGFloat = 0
        if link.kind == .spring {
            let forceMagnitude = max(-6000, min(6000, link.springConstant * stretch * 0.1)) / pixelsPerMeter
            springForceMagnitude = forceMagnitude
            link.freeEndVelocity = link.freeEndVelocity - direction * (forceMagnitude / freeMass * dt)
            if !attached.fixed { attached.velocity = attached.velocity + direction * (forceMagnitude / attached.mass * dt) }
            link.deformation = stretch / max(0.1, restLength)
        } else if stretch > 0 {
            let relative = (link.freeEndVelocity - attached.velocity).dot(direction)
            if relative > 0 {
                let impulse = relative / (1 / freeMass + (attached.fixed ? 0 : 1 / attached.mass))
                link.freeEndVelocity = link.freeEndVelocity - direction * (impulse / freeMass)
                if !attached.fixed { attached.velocity = attached.velocity + direction * (impulse / attached.mass) }
            }
            link.freeEnd = attached.position + direction * restLength
        }
        link.freeEndVelocity = link.freeEndVelocity + link.freeAppliedForce / freeMass * dt
        link.freeEndVelocity.dy += gravity * dt
        link.freeEnd = link.freeEnd + link.freeEndVelocity * (dt * pixelsPerMeter)
        if link.freeEnd.y > groundY - 3 {
            link.freeEnd.y = groundY - 3
            link.freeEndVelocity = .zero
            if link.kind == .spring, stretch < 0, !attached.fixed {
                attached.velocity = attached.velocity - direction * (springForceMagnitude / attached.mass * dt)
            }
        }
        if link.freeEnd.x < wallLeftX + 2 { link.freeEnd.x = wallLeftX + 2; link.freeEndVelocity.dx = max(0, link.freeEndVelocity.dx) }
        if link.freeEnd.x > wallRightX - 2 { link.freeEnd.x = wallRightX - 2; link.freeEndVelocity.dx = min(0, link.freeEndVelocity.dx) }
        if link.kind == .spring {
            for other in objects where other !== link && other !== attached && !isConstraintBody(other) && other.kind != .straightRamp && other.kind != .arcRamp && other.kind != .ring {
                var normal = CGVector.zero
                var penetration: CGFloat = 0
                if other.kind == .ball || other.kind == .pulley {
                    let difference = link.freeEnd - other.position
                    let distance = difference.length
                    let minimum = other.radius + 5
                    if distance < minimum, distance > 0.001 { normal = difference / distance; penetration = minimum - distance }
                } else {
                    let local = rotate(link.freeEnd - other.position, by: -other.rotation)
                    let halfWidth = other.size.width * 0.5 + 5
                    let halfHeight = other.size.height * 0.5 + 5
                    if abs(local.dx) < halfWidth, abs(local.dy) < halfHeight {
                        let values = [local.dx + halfWidth, halfWidth - local.dx, local.dy + halfHeight, halfHeight - local.dy]
                        let minimum = values.enumerated().min(by: { $0.element < $1.element })!
                        normal = rotate([CGVector(dx: -1, dy: 0), CGVector(dx: 1, dy: 0), CGVector(dx: 0, dy: -1), CGVector(dx: 0, dy: 1)][minimum.offset], by: other.rotation)
                        penetration = minimum.element
                    }
                }
                if penetration > 0 {
                    link.freeEnd = link.freeEnd + normal * penetration
                    let inward = link.freeEndVelocity.dot(normal)
                    if inward < 0 { link.freeEndVelocity = link.freeEndVelocity - normal * inward }
                }
            }
        }
        if link.angleFixed {
            let axis = CGVector(dx: cos(link.fixedAngle), dy: sin(link.fixedAngle))
            let direction = attachedIsA ? axis : axis * -1
            let along = max(0.001, (link.freeEnd - attached.position).dot(direction))
            link.freeEnd = attached.position + direction * along
            link.freeEndVelocity = direction * link.freeEndVelocity.dot(direction)
        }
        let finalDelta = link.freeEnd - attached.position
        link.position = CGPoint(x: (attached.position.x + link.freeEnd.x) * 0.5, y: (attached.position.y + link.freeEnd.y) * 0.5)
        let orientedDelta = attachedIsA ? finalDelta : finalDelta * -1
        link.rotation = link.angleFixed ? link.fixedAngle : atan2(orientedDelta.dy, orientedDelta.dx)
        link.horizontal = true
        link.size = CGSize(width: max(1, finalDelta.length), height: link.kind == .spring ? 20 : 10)
    }

    private func solveSingleFixedConstraint(_ link: PhysicsObject, dt: CGFloat) -> Bool {
        let fixedIsA = link.anchorA != .none
        let anchor = fixedIsA ? link.anchorA : link.anchorB
        guard let start = point(for: anchor) else { return false }
        if link.kind == .rod {
            let direction: CGVector
            switch anchor {
            case .ceiling: direction = CGVector(dx: 0, dy: 1)
            case .ground: direction = CGVector(dx: 0, dy: -1)
            case .leftWall: direction = CGVector(dx: 1, dy: 0)
            case .rightWall: direction = CGVector(dx: -1, dy: 0)
            default: direction = CGVector(dx: cos(link.rotation), dy: sin(link.rotation))
            }
            let end = start + direction * link.rodLength
            link.position = CGPoint(x: (start.x + end.x) * 0.5, y: (start.y + end.y) * 0.5)
            let axis = fixedIsA ? direction : direction * -1
            link.rotation = atan2(axis.dy, axis.dx)
            link.size = CGSize(width: link.rodLength, height: 6)
            return false
        }
        let elongation = link.fixed ? 0 : link.mass * gravity / (2 * max(1, link.springConstant)) * pixelsPerMeter
        let direction: CGVector
        var length: CGFloat
        switch anchor {
        case .ceiling:
            direction = CGVector(dx: 0, dy: 1)
            length = (link.kind == .spring ? link.naturalLength : link.ropeLength) + elongation
        case .ground:
            direction = CGVector(dx: 0, dy: -1)
            if link.kind == .rope, !link.fixed {
                if !link.cutDecaying { link.cutDecaying = true; link.cutDecayTimer = max(1, link.ropeLength) }
                link.cutDecayTimer -= 280 * dt
                if link.cutDecayTimer < 1 { return true }
                length = link.cutDecayTimer
            } else {
                link.cutDecaying = false
                link.cutDecayTimer = link.ropeLength
                length = max(1, (link.kind == .spring ? link.naturalLength : link.ropeLength) - elongation)
            }
        case .leftWall:
            direction = CGVector(dx: 1, dy: 0)
            length = link.kind == .spring ? link.naturalLength : max(1, link.ropeLength - elongation * 0.3)
        case .rightWall:
            direction = CGVector(dx: -1, dy: 0)
            length = link.kind == .spring ? link.naturalLength : max(1, link.ropeLength - elongation * 0.3)
        default: return false
        }
        if link.kind == .spring {
            var pressed: PhysicsObject?
            for other in objects where other !== link && !other.fixed && !isConstraintBody(other) && other.kind != .ring && other.kind != .straightRamp && other.kind != .arcRamp {
                let relative = other.position - start
                let along = relative.dot(direction)
                let lateral = abs(relative.dy * direction.dx - relative.dx * direction.dy)
                let halfAlong = abs(direction.dx) > 0.5 ? other.size.width * 0.5 : other.size.height * 0.5
                let halfLateral = abs(direction.dx) > 0.5 ? other.size.height * 0.5 : other.size.width * 0.5
                guard lateral <= halfLateral + 14, along > 0 else { continue }
                let nearDistance = max(8, along - halfAlong)
                if nearDistance < length { length = nearDistance; pressed = other }
            }
            if let pressed, length < link.naturalLength {
                let force = min(6000, link.springConstant * (link.naturalLength - length) * 0.1) / pixelsPerMeter
                pressed.velocity = pressed.velocity + direction * (force / pressed.mass * dt)
                pressed.constraintForce = pressed.constraintForce + direction * force
            }
        }
        let end = start + direction * length
        link.position = CGPoint(x: (start.x + end.x) * 0.5, y: (start.y + end.y) * 0.5)
        let axis = fixedIsA ? direction : direction * -1
        link.rotation = atan2(axis.dy, axis.dx)
        link.size = CGSize(width: length, height: link.kind == .spring ? 20 : 10)
        return false
    }

    private func updateCutHalves(_ dt: CGFloat) {
        var expired: [PhysicsObject] = []
        for half in objects where half.isCutHalf {
            let anchor = half.cutHalfFollowObject?.position ?? half.cutHalfAnchor
            if let follow = half.cutHalfFollowObject, !follow.fixed, abs(half.deformation) > 0.001 {
                let direction = (half.cutHalfFree - follow.position).normalized
                let force = max(-5000 / pixelsPerMeter, min(5000 / pixelsPerMeter, half.deformation))
                follow.velocity = follow.velocity + direction * (force / follow.mass * dt)
            }
            var direction = half.cutHalfFree - anchor
            let length = direction.length
            if length > 0.001 { direction = direction / length }
            half.cutHalfLength -= 250 * dt
            if half.cutHalfLength <= 0 { expired.append(half); continue }
            half.cutHalfFree = anchor + direction * half.cutHalfLength
            half.position = CGPoint(x: (anchor.x + half.cutHalfFree.x) * 0.5, y: (anchor.y + half.cutHalfFree.y) * 0.5)
            half.size = CGSize(width: half.cutHalfLength, height: 20)
            half.rotation = atan2(direction.dy, direction.dx)
            half.horizontal = true
        }
        expired.forEach(remove)
    }

    private func applyLinkForce(_ force: CGVector, to anchor: Anchor, dt: CGFloat) {
        guard case .object(let id) = anchor, let target = objects.first(where: { $0.id == id }), !target.fixed else { return }
        target.velocity = target.velocity + force / max(0.001, target.mass) * dt
        target.reactionForce = target.reactionForce + force
    }

    private func applyRopeSeparationImpulse(_ rope: PhysicsObject, direction: CGVector, tension: CGFloat, dt: CGFloat) {
        let first: PhysicsObject?
        let second: PhysicsObject?
        if case .object(let id) = rope.anchorA { first = objects.first { $0.id == id } } else { first = nil }
        if case .object(let id) = rope.anchorB { second = objects.first { $0.id == id } } else { second = nil }
        let firstVelocity = first?.velocity ?? .zero
        let secondVelocity = second?.velocity ?? .zero
        guard (secondVelocity - firstVelocity).dot(direction) > 0 else { return }
        let impulse = tension * 0.5 * dt
        if let first, !first.fixed { first.velocity = first.velocity + direction * (impulse / first.mass) }
        if let second, !second.fixed { second.velocity = second.velocity - direction * (impulse / second.mass) }
    }

    private func updateWrapPath(_ rope: PhysicsObject) {
        guard let pulley = objects.first(where: { $0.kind == .pulley && distanceToSegment($0.position, endpoints(of: rope).0, endpoints(of: rope).1) < $0.radius + 8 }) else { return }
        let (a, b) = endpoints(of: rope)
        let angleA = atan2(a.y - pulley.position.y, a.x - pulley.position.x)
        let angleB = atan2(b.y - pulley.position.y, b.x - pulley.position.x)
        var sweep = angleB - angleA
        while sweep < 0 { sweep += 2 * .pi }
        if sweep > .pi { sweep -= 2 * .pi }
        var points = [a]
        for index in 0...20 {
            let angle = angleA + sweep * CGFloat(index) / 20
            points.append(CGPoint(x: pulley.position.x + cos(angle) * pulley.radius, y: pulley.position.y + sin(angle) * pulley.radius))
        }
        points.append(b)
        rope.wrapPoints = points
    }

    private func autoWrapRope(_ rope: PhysicsObject) {
        let ends = endpoints(of: rope)
        let line = ends.1 - ends.0
        let lengthSquared = line.dx * line.dx + line.dy * line.dy
        guard lengthSquared >= 1 else { return }
        for pulley in objects where pulley.kind == .pulley {
            let t = ((pulley.position.x - ends.0.x) * line.dx + (pulley.position.y - ends.0.y) * line.dy) / lengthSquared
            guard (0...1).contains(t) else { continue }
            let closest = ends.0 + line * t
            if hypot(pulley.position.x - closest.x, pulley.position.y - closest.y) <= pulley.radius + 18 {
                buildPulleyWrapPath(rope, pulley: pulley, start: ends.0, end: ends.1)
                return
            }
        }
    }

    private func buildPulleyWrapPath(_ rope: PhysicsObject, pulley: PhysicsObject, start: CGPoint, end: CGPoint) {
        let radius = pulley.radius + 8
        let firstAngle = atan2(start.y - pulley.position.y, start.x - pulley.position.x)
        let secondAngle = atan2(end.y - pulley.position.y, end.x - pulley.position.x)
        var clockwise = secondAngle - firstAngle
        while clockwise > 0 { clockwise -= 2 * .pi }
        while clockwise < -2 * .pi { clockwise += 2 * .pi }
        var counterclockwise = secondAngle - firstAngle
        while counterclockwise < 0 { counterclockwise += 2 * .pi }
        while counterclockwise > 2 * .pi { counterclockwise -= 2 * .pi }
        let clockwiseMidY = pulley.position.y + sin(firstAngle + clockwise * 0.5) * radius
        let counterclockwiseMidY = pulley.position.y + sin(firstAngle + counterclockwise * 0.5) * radius
        let arc = clockwiseMidY < counterclockwiseMidY ? clockwise : counterclockwise
        let steps = max(8, Int(abs(arc) * radius / 12))
        rope.wrapPoints = [start]
        for index in 0...steps {
            let angle = firstAngle + arc * CGFloat(index) / CGFloat(steps)
            rope.wrapPoints.append(CGPoint(x: pulley.position.x + cos(angle) * radius, y: pulley.position.y + sin(angle) * radius))
        }
        rope.wrapPoints.append(end)
        rope.ropeParticles.removeAll()
        rope.ropeWrapped = true
        rope.ropeParticlesInitialized = false
    }

    private func densifyWrappedRope(_ rope: PhysicsObject) {
        guard rope.wrapPoints.count >= 2 else { return }
        let maximumSegment = max(6, pixelsPerMeter * 0.06)
        var dense: [CGPoint] = []
        var totalLength: CGFloat = 0
        for index in 0..<(rope.wrapPoints.count - 1) {
            let start = rope.wrapPoints[index]
            let end = rope.wrapPoints[index + 1]
            if index == 0 { dense.append(start) }
            let delta = end - start
            let length = delta.length
            totalLength += length
            let steps = max(1, Int(ceil(length / maximumSegment)))
            for step in 1...steps { dense.append(start + delta * (CGFloat(step) / CGFloat(steps))) }
        }
        if rope.ropeLength < totalLength { rope.ropeLength = totalLength }
        rope.wrapPoints = dense
        rope.ropeParticles = dense.map { RopeParticle(position: $0, velocity: .zero) }
        rope.ropeParticlesInitialized = true
    }

    private func anchorWrappedRopeEndpoints(_ rope: PhysicsObject) {
        guard rope.ropeParticles.count >= 2 else { return }
        if let point = point(for: rope.anchorA) {
            rope.ropeParticles[0].position = surfacePoint(for: rope.anchorA, toward: rope.ropeParticles[1].position) ?? point
            rope.ropeParticles[0].velocity = .zero
        }
        if let point = point(for: rope.anchorB) {
            let last = rope.ropeParticles.count - 1
            rope.ropeParticles[last].position = surfacePoint(for: rope.anchorB, toward: rope.ropeParticles[last - 1].position) ?? point
            rope.ropeParticles[last].velocity = .zero
        }
    }

    private func surfacePoint(for anchor: Anchor, toward: CGPoint) -> CGPoint? {
        guard case .object(let id) = anchor, let object = objects.first(where: { $0.id == id }) else { return point(for: anchor) }
        let direction = (toward - object.position).normalized
        if object.kind == .ball || object.kind == .pulley || object.kind == .ring { return object.position + direction * object.radius }
        let halfWidth = object.size.width * 0.5
        let halfHeight = object.size.height * 0.5
        let localDirection = rotate(direction, by: -object.rotation)
        let scaleX = abs(localDirection.dx) > 0.0001 ? halfWidth / abs(localDirection.dx) : .greatestFiniteMagnitude
        let scaleY = abs(localDirection.dy) > 0.0001 ? halfHeight / abs(localDirection.dy) : .greatestFiniteMagnitude
        return object.position + rotate(localDirection * min(scaleX, scaleY), by: object.rotation)
    }

    private func solveWrappedRopes(_ dt: CGFloat) {
        for rope in objects where rope.kind == .rope && rope.ropeWrapped && !rope.wrapPoints.isEmpty {
            if !rope.ropeParticlesInitialized {
                densifyWrappedRope(rope)
                for index in rope.ropeParticles.indices { collideRopeParticle(rope, index: index) }
            }
            let count = rope.ropeParticles.count
            guard count >= 2 else { continue }
            anchorWrappedRopeEndpoints(rope)
            let firstAnchored = rope.anchorA != .none
            let lastAnchored = rope.anchorB != .none
            for index in rope.ropeParticles.indices {
                if index == 0 && firstAnchored || index == count - 1 && lastAnchored { continue }
                rope.ropeParticles[index].velocity = rope.ropeParticles[index].velocity * 0.98
                rope.ropeParticles[index].velocity.dy += gravity * pixelsPerMeter * dt
                rope.ropeParticles[index].position = rope.ropeParticles[index].position + rope.ropeParticles[index].velocity * dt
                collideRopeParticle(rope, index: index)
            }
            collideRopeSegments(rope, firstAnchored: firstAnchored, lastAnchored: lastAnchored)
            var segmentLengths: [CGFloat] = []
            var totalLength: CGFloat = 0
            for index in 0..<(count - 1) {
                let length = (rope.ropeParticles[index + 1].position - rope.ropeParticles[index].position).length
                segmentLengths.append(length)
                totalLength += length
            }
            let lengthScale = totalLength > rope.ropeLength && totalLength > 0.001 ? rope.ropeLength / totalLength : 1
            for _ in 0..<20 {
                anchorWrappedRopeEndpoints(rope)
                for index in 0..<(count - 1) {
                    let delta = rope.ropeParticles[index + 1].position - rope.ropeParticles[index].position
                    let currentLength = delta.length
                    guard currentLength >= 0.001 else { continue }
                    let difference = (currentLength - segmentLengths[index] * lengthScale) / currentLength * 0.5
                    if !(index == 0 && firstAnchored) { rope.ropeParticles[index].position = rope.ropeParticles[index].position + delta * difference }
                    if !(index == count - 2 && lastAnchored) { rope.ropeParticles[index + 1].position = rope.ropeParticles[index + 1].position + delta * -difference }
                }
                for index in rope.ropeParticles.indices where !(index == 0 && firstAnchored) && !(index == count - 1 && lastAnchored) { collideRopeParticle(rope, index: index) }
                collideRopeSegments(rope, firstAnchored: firstAnchored, lastAnchored: lastAnchored)
            }
            anchorWrappedRopeEndpoints(rope)
            collideRopeSegments(rope, firstAnchored: firstAnchored, lastAnchored: lastAnchored)
            rope.wrapPoints = rope.ropeParticles.map(\.position)
            if let first = rope.wrapPoints.first, let last = rope.wrapPoints.last { rope.position = CGPoint(x: (first.x + last.x) * 0.5, y: (first.y + last.y) * 0.5) }
        }
    }

    private func collideRopeParticle(_ rope: PhysicsObject, index: Int) {
        let radius: CGFloat = 5
        var particle = rope.ropeParticles[index]
        if particle.position.y > groundY - radius {
            particle.position.y = groundY - radius
            if particle.velocity.dy > 0 { particle.velocity.dy = -particle.velocity.dy * 0.05 }
            particle.velocity.dx *= 0.6
        }
        if particle.position.x < wallLeftX + radius { particle.position.x = wallLeftX + radius; if particle.velocity.dx < 0 { particle.velocity.dx = 0 } }
        if particle.position.x > wallRightX - radius { particle.position.x = wallRightX - radius; if particle.velocity.dx > 0 { particle.velocity.dx = 0 } }
        for other in objects where other !== rope && ![.spring, .rope, .rod, .ring].contains(other.kind) {
            var normal = CGVector.zero
            var penetration: CGFloat = 0
            if other.kind == .ball || other.kind == .pulley {
                let delta = particle.position - other.position
                let distance = delta.length
                let minimum = other.radius + radius + (other.kind == .pulley ? 3 : 0)
                if distance < minimum { normal = distance < 0.001 ? CGVector(dx: 0, dy: -1) : delta / distance; penetration = minimum - distance }
            } else if other.kind == .straightRamp {
                let left = other.position.x - other.size.width * 0.5
                let right = other.position.x + other.size.width * 0.5
                if particle.position.x >= left - radius, particle.position.x <= right + radius {
                    let fraction = max(0, min(1, (particle.position.x - left) / max(1, other.size.width)))
                    let effective = other.horizontal ? fraction : 1 - fraction
                    let surfaceY = other.position.y + other.size.height * 0.5 - effective * other.size.height
                    let slope = other.horizontal ? -other.size.height / max(1, other.size.width) : other.size.height / max(1, other.size.width)
                    let angle = atan(slope)
                    normal = CGVector(dx: sin(angle), dy: -cos(angle))
                    let signed = (particle.position.y - surfaceY) * normal.dy
                    if signed < radius, particle.position.y > surfaceY - other.size.height - radius { penetration = radius - signed }
                }
            } else if other.kind == .arcRamp {
                if let contact = arcContact(ramp: other, point: particle.position) {
                    let distance = (particle.position - contact.center).length
                    let target = contact.radius - radius
                    if distance > target, distance - target < max(40, radius * 4) { normal = contact.radial * -1; penetration = distance - target }
                }
            } else {
                let local = rotate(particle.position - other.position, by: -other.rotation)
                let halfWidth = other.size.width * 0.5 + radius
                let halfHeight = other.size.height * 0.5 + radius
                if abs(local.dx) < halfWidth, abs(local.dy) < halfHeight {
                    let distances = [local.dx + halfWidth, halfWidth - local.dx, local.dy + halfHeight, halfHeight - local.dy]
                    let smallest = distances.enumerated().min(by: { $0.element < $1.element })!
                    let localNormal: CGVector = [CGVector(dx: -1, dy: 0), CGVector(dx: 1, dy: 0), CGVector(dx: 0, dy: -1), CGVector(dx: 0, dy: 1)][smallest.offset]
                    normal = rotate(localNormal, by: other.rotation)
                    penetration = smallest.element
                }
            }
            if penetration > 0 {
                particle.position = particle.position + normal * penetration
                let velocityNormal = particle.velocity.dot(normal)
                if velocityNormal < 0 { particle.velocity = particle.velocity - normal * velocityNormal }
            }
        }
        rope.ropeParticles[index] = particle
    }

    private func collideRopeSegments(_ rope: PhysicsObject, firstAnchored: Bool, lastAnchored: Bool) {
        guard rope.ropeParticles.count >= 2 else { return }
        for index in 0..<(rope.ropeParticles.count - 1) {
            let fixedA = index == 0 && firstAnchored
            let fixedB = index == rope.ropeParticles.count - 2 && lastAnchored
            for other in objects where other.kind == .pulley || other.kind == .ball {
                var first = rope.ropeParticles[index]
                var second = rope.ropeParticles[index + 1]
                let segment = second.position - first.position
                let lengthSquared = segment.dx * segment.dx + segment.dy * segment.dy
                guard lengthSquared >= 0.001 else { continue }
                let t = max(0, min(1, ((other.position.x - first.position.x) * segment.dx + (other.position.y - first.position.y) * segment.dy) / lengthSquared))
                let closest = first.position + segment * t
                let delta = closest - other.position
                let distance = delta.length
                let minimum = other.radius + 7
                guard distance < minimum else { continue }
                let normal = distance < 0.001 ? CGVector(dx: -segment.dy, dy: segment.dx).normalized : delta / distance
                let push = minimum - distance
                if !fixedA && !fixedB {
                    first.position = first.position + normal * (push * (1 - t))
                    second.position = second.position + normal * (push * t)
                } else if !fixedA { first.position = first.position + normal * push } else if !fixedB { second.position = second.position + normal * push }
                let velocityA = first.velocity.dot(normal)
                if !fixedA, velocityA < 0 { first.velocity = first.velocity - normal * velocityA }
                let velocityB = second.velocity.dot(normal)
                if !fixedB, velocityB < 0 { second.velocity = second.velocity - normal * velocityB }
                rope.ropeParticles[index] = first
                rope.ropeParticles[index + 1] = second
            }
        }
    }

    private func rotate(_ vector: CGVector, by angle: CGFloat) -> CGVector {
        CGVector(dx: vector.dx * cos(angle) - vector.dy * sin(angle), dy: vector.dx * sin(angle) + vector.dy * cos(angle))
    }

    private func solveRings(_ dt: CGFloat) {
        for ring in objects where ring.kind == .ring {
            guard let rod = ring.ringHostRod, objects.contains(where: { $0 === rod }) else { continue }
            let (a, b) = endpoints(of: rod)
            let axis = b - a
            let length = max(0.001, axis.length)
            let unit = axis / length
            var parameter = ((ring.position.x - a.x) * axis.dx + (ring.position.y - a.y) * axis.dy) / (length * length)
            parameter = max(0, min(1, parameter))
            let projected = CGPoint(x: a.x + axis.dx * parameter, y: a.y + axis.dy * parameter)
            let normal = CGVector(dx: -unit.dy, dy: unit.dx)
            let perpendicularForce = ring.displayForce.dot(normal)
            var normalForce = normal * -perpendicularForce
            let oldDistance = (projected - ring.position).length
            if normalForce.length < 0.001, oldDistance > 0.001 { normalForce = (projected - ring.position).normalized * min(8000, oldDistance / max(0.001, dt * dt) / pixelsPerMeter * ring.mass) }
            ring.supportForce = ring.supportForce + normalForce
            var alongVelocity = ring.velocity.dot(unit)
            let frictionLimit = ring.friction * normalForce.length
            let tangentForce = ring.displayForce.dot(unit)
            if frictionLimit > 0.001 {
                let friction = abs(alongVelocity) > 0.01 ? (alongVelocity > 0 ? -frictionLimit : frictionLimit) : max(-frictionLimit, min(frictionLimit, -tangentForce))
                let previous = alongVelocity
                alongVelocity += friction / ring.mass * dt
                if previous > 0 && alongVelocity < 0 || previous < 0 && alongVelocity > 0 { alongVelocity = 0 }
                ring.frictionForce = ring.frictionForce + unit * friction
            }
            if parameter <= 0, alongVelocity < 0 { alongVelocity = 0 }
            if parameter >= 1, alongVelocity > 0 { alongVelocity = 0 }
            ring.ringParameter = parameter
            ring.position = projected
            ring.velocity = unit * alongVelocity
            rod.rodRing = ring
        }
    }

    private func updateBoundPulleys() {
        for pulley in objects where pulley.kind == .pulley {
            guard let ramp = pulley.pulleyRamp, objects.contains(where: { $0 === ramp }) else { continue }
            pulley.position = CGPoint(x: ramp.position.x + (ramp.horizontal ? ramp.size.width * 0.5 : -ramp.size.width * 0.5), y: ramp.position.y - ramp.size.height * 0.5)
            pulley.velocity = .zero
            pulley.fixed = true
        }
    }

    private func solveWorldCollisions(_ dt: CGFloat) {
        for object in objects where !object.fixed && ![.spring, .rope, .rod, .ring].contains(object.kind) {
            var bounds = worldBounds(object)
            if bounds.maxY > groundY {
                object.position.y -= bounds.maxY - groundY
                if object.velocity.dy > 0 { object.velocity.dy = object.velocity.dy <= gravity * dt * 1.5 ? 0 : -object.velocity.dy * object.restitution }
                let normal = max(0, object.displayForce.dy) + stackedMass(on: object) * gravity
                object.supportForce.dy = -normal
                object.contactGround = true
                if !isGroundErased(at: object.position.x) { applyFriction(to: object, normal: object.kind == .ball ? normal * 0.5 : normal, tangent: CGVector(dx: 1, dy: 0), dt: dt) }
            }
            bounds = worldBounds(object)
            if bounds.minX < wallLeftX {
                object.position.x += wallLeftX - bounds.minX
                if object.velocity.dx < 0 {
                    let threshold = abs(object.displayForce.dx) / object.mass * dt * 1.5
                    object.velocity.dx = -object.velocity.dx <= threshold ? 0 : -object.velocity.dx * object.restitution
                }
                object.contactLeftWall = true
            }
            bounds = worldBounds(object)
            if bounds.maxX > wallRightX {
                object.position.x -= bounds.maxX - wallRightX
                if object.velocity.dx > 0 {
                    let threshold = abs(object.displayForce.dx) / object.mass * dt * 1.5
                    object.velocity.dx = object.velocity.dx <= threshold ? 0 : -object.velocity.dx * object.restitution
                }
                object.contactRightWall = true
            }
            bounds = worldBounds(object)
            for index in ceilings.indices {
                let segment = ceilings[index]
                if isObject(object, linkedByRodToCeiling: index) { continue }
                let minimumX = min(segment.start.x, segment.end.x)
                let maximumX = max(segment.start.x, segment.end.x)
                if bounds.maxX >= minimumX, bounds.minX <= maximumX, bounds.minY <= segment.start.y + 2 {
                    object.position.y += segment.start.y + 1 - bounds.minY
                    if object.velocity.dy < 0 { object.velocity.dy = -object.velocity.dy * object.restitution }
                }
            }
        }
    }

    private func stackedMass(on object: PhysicsObject) -> CGFloat {
        let bounds = worldBounds(object)
        var result: CGFloat = 0
        for other in objects where other !== object && !other.fixed && !isConstraintBody(other) && other.kind != .ring {
            let otherBounds = worldBounds(other)
            let overlap = min(bounds.maxX, otherBounds.maxX) - max(bounds.minX, otherBounds.minX)
            if (object.kind == .straightRamp || object.kind == .arcRamp), other.rampContact === object { result += other.mass }
            else if overlap > 2, abs(otherBounds.maxY - bounds.minY) < 12 { result += other.mass }
        }
        return result
    }

    private func isObject(_ object: PhysicsObject, linkedByRodToCeiling index: Int) -> Bool {
        objects.contains { rod in
            guard rod.kind == .rod else { return false }
            let hasCeiling: Bool
            if case .ceiling(let value, _) = rod.anchorA { hasCeiling = value == index }
            else if case .ceiling(let value, _) = rod.anchorB { hasCeiling = value == index }
            else { hasCeiling = false }
            guard hasCeiling else { return false }
            if case .object(let id) = rod.anchorA, id == object.id { return true }
            if case .object(let id) = rod.anchorB, id == object.id { return true }
            return false
        }
    }

    private func solveSpringBodyCollisions(_ dt: CGFloat) {
        for spring in objects where spring.kind == .spring && !spring.isCutHalf {
            let ends = endpoints(of: spring)
            let segment = ends.1 - ends.0
            let lengthSquared = segment.dx * segment.dx + segment.dy * segment.dy
            guard lengthSquared >= 100 else { continue }
            let halfThickness = spring.size.height * 0.5 + 6
            for object in objects where object !== spring && !isConstraintBody(object) && object.kind != .ring && !object.fixed && object.kind != .straightRamp && object.kind != .arcRamp {
                if case .object(let id) = spring.anchorA, id == object.id { continue }
                if case .object(let id) = spring.anchorB, id == object.id { continue }
                let objectRadius = object.kind == .ball ? object.radius : min(object.size.width, object.size.height) * 0.45
                let minimumDistance = halfThickness + objectRadius
                let t = max(0.05, min(0.95, ((object.position.x - ends.0.x) * segment.dx + (object.position.y - ends.0.y) * segment.dy) / lengthSquared))
                let closest = ends.0 + segment * t
                let delta = object.position - closest
                let distance = delta.length
                guard distance < minimumDistance, distance >= 0.0001 else { continue }
                let normal = delta / distance
                let penetration = minimumDistance - distance
                object.position = object.position + normal * penetration
                let normalVelocity = object.velocity.dot(normal)
                if normalVelocity < 0 { object.velocity = object.velocity - normal * ((1 + object.restitution * 0.3) * normalVelocity) }
                let pushVelocity = penetration * 0.25 / max(0.001, dt) / pixelsPerMeter
                if case .object(let id) = spring.anchorA, let attached = objects.first(where: { $0.id == id && !$0.fixed }) { attached.velocity = attached.velocity - normal * (pushVelocity * (1 - t) * 0.12) }
                if case .object(let id) = spring.anchorB, let attached = objects.first(where: { $0.id == id && !$0.fixed }) { attached.velocity = attached.velocity - normal * (pushVelocity * t * 0.12) }
            }
        }
    }

    private func applyFriction(to object: PhysicsObject, normal: CGFloat, tangent: CGVector, dt: CGFloat) {
        let speed = object.velocity.dot(tangent)
        let maxChange = object.friction * normal / max(0.001, object.mass) * dt
        let change = min(abs(speed), maxChange) * (speed < 0 ? 1 : -1)
        object.velocity = object.velocity + tangent * change
        object.frictionForce = tangent * (change * object.mass / max(dt, 0.001))
    }

    private func solvePairCollisions(_ dt: CGFloat) {
        guard objects.count > 1 else { return }
        for _ in 0..<8 {
            for firstIndex in 0..<(objects.count - 1) {
                for secondIndex in (firstIndex + 1)..<objects.count {
                let a = objects[firstIndex]
                let b = objects[secondIndex]
                if a.fixed && b.fixed { continue }
                if [.spring, .rope, .rod, .ring, .straightRamp, .arcRamp, .container].contains(a.kind) || [.spring, .rope, .rod, .ring, .straightRamp, .arcRamp, .container].contains(b.kind) { continue }
                let circularA = (a.kind == .ball || a.kind == .pulley) && a.polygon.isEmpty
                let circularB = (b.kind == .ball || b.kind == .pulley) && b.polygon.isEmpty
                if circularA && circularB {
                    resolveCircularPair(a, b)
                } else if circularA {
                    resolveCircleBox(circle: a, box: b)
                } else if circularB {
                    resolveCircleBox(circle: b, box: a)
                } else {
                    resolveOrientedBoxes(a, b)
                }
                }
            }
        }
    }

    private func resolveCircularPair(_ a: PhysicsObject, _ b: PhysicsObject) {
        let delta = b.position - a.position
        let distance = max(0.001, delta.length)
        let radiusA = a.kind == .ball || a.kind == .pulley ? a.radius : min(a.size.width, a.size.height) * 0.5
        let radiusB = b.kind == .ball || b.kind == .pulley ? b.radius : min(b.size.width, b.size.height) * 0.5
        let overlap = radiusA + radiusB - distance
        guard overlap > 0 else { return }
        let normal = delta / distance
        separate(a, b, overlap: overlap, normal: normal)
        let speedA = a.velocity.dot(normal)
        let speedB = b.velocity.dot(normal)
        guard speedA - speedB > 0 else { return }
        let inverseA: CGFloat = a.fixed ? 0 : 1 / max(0.001, a.mass)
        let inverseB: CGFloat = b.fixed ? 0 : 1 / max(0.001, b.mass)
        let inverseTotal = inverseA + inverseB
        guard inverseTotal > 0 else { return }
        let normalImpulse = (1 + min(a.restitution, b.restitution)) * (speedA - speedB) / inverseTotal
        if !a.fixed { a.velocity = a.velocity - normal * (normalImpulse * inverseA) }
        if !b.fixed { b.velocity = b.velocity + normal * (normalImpulse * inverseB) }
        let tangent = CGVector(dx: -normal.dy, dy: normal.dx)
        let relativeTangent = (a.velocity - b.velocity).dot(tangent)
        let top = a.position.y < b.position.y ? a : b
        let frictionImpulse = max(-top.friction * normalImpulse, min(top.friction * normalImpulse, -relativeTangent / inverseTotal))
        if !a.fixed { a.velocity = a.velocity + tangent * (frictionImpulse * inverseA) }
        if !b.fixed { b.velocity = b.velocity - tangent * (frictionImpulse * inverseB) }
        recordContactImpulse(a, b, normal: normal * -1, normalImpulse: normalImpulse, tangent: tangent, frictionImpulse: frictionImpulse)
    }

    private func resolveAABB(_ a: PhysicsObject, _ b: PhysicsObject) {
        let intersection = worldBounds(a).intersection(worldBounds(b))
        guard !intersection.isNull, intersection.width > 0, intersection.height > 0 else { return }
        let axis: CGVector
        let penetration: CGFloat
        if intersection.width < intersection.height {
            axis = CGVector(dx: a.position.x < b.position.x ? -1 : 1, dy: 0)
            penetration = intersection.width
        } else {
            axis = CGVector(dx: 0, dy: a.position.y < b.position.y ? -1 : 1)
            penetration = intersection.height
        }
        var inverseA: CGFloat = a.fixed ? 0 : 1 / max(0.001, a.mass)
        var inverseB: CGFloat = b.fixed ? 0 : 1 / max(0.001, b.mass)
        let tangentInverseA = inverseA
        let tangentInverseB = inverseB
        if axis.dy != 0 {
            let lower = a.position.y > b.position.y ? a : b
            if lower.contactGround {
                if lower === a { inverseA = 0 } else { inverseB = 0 }
            }
        }
        let inverseTotal = inverseA + inverseB
        guard inverseTotal > 0 else { return }
        if !a.fixed { a.position = a.position + axis * (penetration * inverseA / inverseTotal) }
        if !b.fixed { b.position = b.position - axis * (penetration * inverseB / inverseTotal) }
        var normalImpulse: CGFloat = 0
        let relativeNormal = (a.velocity - b.velocity).dot(axis)
        if relativeNormal < 0 {
            var restitution = min(a.restitution, b.restitution)
            if axis.dy != 0, -relativeNormal <= gravity * Self.fixedTimeStep * 1.5 { restitution = 0 }
            normalImpulse = -(1 + restitution) * relativeNormal / inverseTotal
            if !a.fixed { a.velocity = a.velocity + axis * (normalImpulse * inverseA) }
            if !b.fixed { b.velocity = b.velocity - axis * (normalImpulse * inverseB) }
        }
        var frictionImpulse: CGFloat = 0
        let tangent = CGVector(dx: -axis.dy, dy: axis.dx)
        if axis.dy != 0, normalImpulse > 0 {
            let top = axis.dy < 0 ? a : b
            let tangentTotal = tangentInverseA + tangentInverseB
            if tangentTotal > 0 {
                let relativeTangent = (a.velocity - b.velocity).dot(tangent)
                frictionImpulse = max(-top.friction * normalImpulse, min(top.friction * normalImpulse, -relativeTangent / tangentTotal))
                if !a.fixed { a.velocity = a.velocity + tangent * (frictionImpulse * tangentInverseA) }
                if !b.fixed { b.velocity = b.velocity - tangent * (frictionImpulse * tangentInverseB) }
            }
        }
        if normalImpulse > 0 { recordContactImpulse(a, b, normal: axis, normalImpulse: normalImpulse, tangent: tangent, frictionImpulse: frictionImpulse) }
    }

    private func worldBounds(_ object: PhysicsObject) -> CGRect {
        guard !object.polygon.isEmpty || abs(object.rotation) > 0.0001 else { return object.bounds }
        let points = object.polygon.isEmpty ? [CGPoint(x: -object.size.width * 0.5, y: -object.size.height * 0.5), CGPoint(x: object.size.width * 0.5, y: -object.size.height * 0.5), CGPoint(x: object.size.width * 0.5, y: object.size.height * 0.5), CGPoint(x: -object.size.width * 0.5, y: object.size.height * 0.5)] : object.polygon
        let world = points.map { localToWorld($0, object: object) }
        return CGRect(x: world.map(\.x).min() ?? object.position.x, y: world.map(\.y).min() ?? object.position.y, width: (world.map(\.x).max() ?? object.position.x) - (world.map(\.x).min() ?? object.position.x), height: (world.map(\.y).max() ?? object.position.y) - (world.map(\.y).min() ?? object.position.y))
    }

    private func resolveOrientedBoxes(_ a: PhysicsObject, _ b: PhysicsObject) {
        if !a.polygon.isEmpty || !b.polygon.isEmpty { resolveAABB(a, b); return }
        if abs(a.rotation) < 0.001, abs(b.rotation) < 0.001 { resolveAABB(a, b); return }
        let angle: CGFloat
        if abs(a.rotation) >= 0.001, abs(b.rotation) >= 0.001 {
            guard abs(a.rotation - b.rotation) <= 0.35 else { resolveAABB(a, b); return }
            angle = (a.rotation + b.rotation) * 0.5
        } else { angle = abs(a.rotation) >= 0.001 ? a.rotation : b.rotation }
        let tangent = CGVector(dx: cos(angle), dy: sin(angle))
        let normalBase = CGVector(dx: sin(angle), dy: -cos(angle))
        let delta = a.position - b.position
        let relativeTangent = delta.dot(tangent)
        let relativeNormal = delta.dot(normalBase)
        let overlapTangent = (a.size.width + b.size.width) * 0.5 - abs(relativeTangent)
        let overlapNormal = (a.size.height + b.size.height) * 0.5 - abs(relativeNormal)
        guard overlapTangent > 0, overlapNormal > 0 else { return }
        let useNormal = overlapNormal <= overlapTangent
        let axis = useNormal ? normalBase * (relativeNormal >= 0 ? 1 : -1) : tangent * (relativeTangent >= 0 ? 1 : -1)
        let penetration = useNormal ? overlapNormal : overlapTangent
        separateAlongAxis(a, b, axis: axis, penetration: penetration)
        let normalImpulse = collisionImpulse(a, b, axis: axis)
        if useNormal {
            let top = relativeNormal >= 0 ? a : b
            let surface = relativeNormal >= 0 ? b : a
            if !top.fixed, top.kind != .ball, top.kind != .pulley {
                top.rotation = surface.rotation
                top.contactRamp = true
                top.rampContact = surface
            }
            applyCollisionFriction(a, b, normal: axis, tangent: tangent, normalImpulse: normalImpulse, coefficient: top.friction)
        }
    }

    private func resolveCircleBox(circle: PhysicsObject, box: PhysicsObject) {
        let relative = circle.position - box.position
        let local = rotate(relative, by: -box.rotation)
        let closest = CGPoint(x: max(-box.size.width * 0.5, min(box.size.width * 0.5, local.dx)), y: max(-box.size.height * 0.5, min(box.size.height * 0.5, local.dy)))
        let difference = CGVector(dx: local.dx - closest.x, dy: local.dy - closest.y)
        let distanceSquared = difference.dx * difference.dx + difference.dy * difference.dy
        guard distanceSquared < circle.radius * circle.radius else { return }
        let localNormal: CGVector
        let overlap: CGFloat
        if distanceSquared > 0.0001 {
            let distance = sqrt(distanceSquared)
            localNormal = difference / distance
            overlap = circle.radius - distance
        } else {
            let distances = [local.dx + box.size.width * 0.5, box.size.width * 0.5 - local.dx, local.dy + box.size.height * 0.5, box.size.height * 0.5 - local.dy]
            let smallest = distances.enumerated().min(by: { $0.element < $1.element })!
            localNormal = [CGVector(dx: -1, dy: 0), CGVector(dx: 1, dy: 0), CGVector(dx: 0, dy: -1), CGVector(dx: 0, dy: 1)][smallest.offset]
            overlap = smallest.element + circle.radius
        }
        let normal = rotate(localNormal, by: box.rotation)
        separateAlongAxis(circle, box, axis: normal, penetration: overlap)
        let normalImpulse = collisionImpulse(circle, box, axis: normal)
        if abs(normal.dy) > 0.5, normalImpulse > 0 {
            applyCollisionFriction(circle, box, normal: normal, tangent: CGVector(dx: -normal.dy, dy: normal.dx), normalImpulse: normalImpulse, coefficient: normal.dy < 0 ? circle.friction : box.friction)
        }
    }

    private func alignObjectsOnRotatedSurfaces() {
        for top in objects {
            if top.fixed || isConstraintBody(top) || top.kind == .ball || top.kind == .pulley || top.kind == .ring || top.kind == .straightRamp || top.kind == .arcRamp { continue }
            for surface in objects where surface !== top {
                if isConstraintBody(surface) || surface.kind == .ball || surface.kind == .pulley || surface.kind == .ring || surface.kind == .straightRamp || surface.kind == .arcRamp || abs(surface.rotation) < 0.001 { continue }
                let tangent = CGVector(dx: cos(surface.rotation), dy: sin(surface.rotation))
                let normal = CGVector(dx: sin(surface.rotation), dy: -cos(surface.rotation))
                let delta = top.position - surface.position
                let relativeTangent = delta.dot(tangent)
                let relativeNormal = delta.dot(normal)
                let topNormalExtent = abs(normal.dx) * top.size.width * 0.5 + abs(normal.dy) * top.size.height * 0.5
                let topTangentExtent = abs(tangent.dx) * top.size.width * 0.5 + abs(tangent.dy) * top.size.height * 0.5
                let targetNormal = surface.size.height * 0.5 + topNormalExtent
                let tangentLimit = max(0, surface.size.width * 0.5 - topTangentExtent * 0.45)
                if abs(relativeTangent) <= tangentLimit, relativeNormal > 0, abs(relativeNormal - targetNormal) <= 28 {
                    top.rotation = surface.rotation
                    top.contactRamp = true
                    top.rampContact = surface
                    break
                }
            }
        }
    }

    private func resetUnsupportedRotations() {
        for object in objects where !object.fixed && !object.contactRamp && object.kind != .spring && object.kind != .rope && object.kind != .ball && object.kind != .rod && object.kind != .ring {
            object.rotation = 0
        }
    }

    private func separateAlongAxis(_ a: PhysicsObject, _ b: PhysicsObject, axis: CGVector, penetration: CGFloat) {
        let inverseA: CGFloat = a.fixed ? 0 : 1 / max(0.001, a.mass)
        let inverseB: CGFloat = b.fixed ? 0 : 1 / max(0.001, b.mass)
        let total = inverseA + inverseB
        guard total > 0 else { return }
        if !a.fixed { a.position = a.position + axis * (penetration * inverseA / total) }
        if !b.fixed { b.position = b.position + axis * (-penetration * inverseB / total) }
    }

    @discardableResult private func collisionImpulse(_ a: PhysicsObject, _ b: PhysicsObject, axis: CGVector) -> CGFloat {
        let relative = (a.velocity - b.velocity).dot(axis)
        guard relative < 0 else { return 0 }
        let inverseA: CGFloat = a.fixed ? 0 : 1 / max(0.001, a.mass)
        let inverseB: CGFloat = b.fixed ? 0 : 1 / max(0.001, b.mass)
        var restitution = min(a.restitution, b.restitution)
        if abs(relative) < 0.35 { restitution = 0 }
        let impulse = -(1 + restitution) * relative / max(0.001, inverseA + inverseB)
        if !a.fixed { a.velocity = a.velocity + axis * (impulse * inverseA) }
        if !b.fixed { b.velocity = b.velocity + axis * (-impulse * inverseB) }
        recordContactImpulse(a, b, normal: axis, normalImpulse: impulse, tangent: CGVector(dx: -axis.dy, dy: axis.dx), frictionImpulse: 0)
        return impulse
    }

    private func applyCollisionFriction(_ a: PhysicsObject, _ b: PhysicsObject, normal: CGVector, tangent: CGVector, normalImpulse: CGFloat, coefficient: CGFloat) {
        guard normalImpulse > 0 else { return }
        let inverseA: CGFloat = a.fixed ? 0 : 1 / max(0.001, a.mass)
        let inverseB: CGFloat = b.fixed ? 0 : 1 / max(0.001, b.mass)
        let relative = (a.velocity - b.velocity).dot(tangent)
        let impulse = max(-coefficient * normalImpulse, min(coefficient * normalImpulse, -relative / max(0.001, inverseA + inverseB)))
        if !a.fixed { a.velocity = a.velocity + tangent * (impulse * inverseA) }
        if !b.fixed { b.velocity = b.velocity + tangent * (-impulse * inverseB) }
        recordContactImpulse(a, b, normal: normal, normalImpulse: 0, tangent: tangent, frictionImpulse: impulse)
    }

    private func recordContactImpulse(_ a: PhysicsObject, _ b: PhysicsObject, normal: CGVector, normalImpulse: CGFloat, tangent: CGVector, frictionImpulse: CGFloat) {
        let duration = Self.fixedTimeStep
        let normalForce = normal * (normalImpulse / max(0.0001, duration))
        let frictionForce = tangent * (frictionImpulse / max(0.0001, duration))
        if !a.fixed { a.supportForce = a.supportForce + normalForce; a.frictionForce = a.frictionForce + frictionForce }
        if !b.fixed { b.supportForce = b.supportForce - normalForce; b.frictionForce = b.frictionForce - frictionForce }
    }

    private func separate(_ a: PhysicsObject, _ b: PhysicsObject, overlap: CGFloat, normal: CGVector) {
        let inverseA: CGFloat = a.fixed ? 0 : 1 / max(0.001, a.mass)
        let inverseB: CGFloat = b.fixed ? 0 : 1 / max(0.001, b.mass)
        let total = inverseA + inverseB
        guard total > 0 else { return }
        if !a.fixed { a.position = a.position + normal * (-overlap * inverseA / total) }
        if !b.fixed { b.position = b.position + normal * (overlap * inverseB / total) }
    }

    private func solveRampCollisions(_ dt: CGFloat) {
        let ramps = objects.filter { $0.kind == .straightRamp || $0.kind == .arcRamp }
        for ramp in ramps {
            for object in objects where object !== ramp && !object.fixed && ![.spring, .rope, .rod, .ring, .container, .straightRamp, .arcRamp].contains(object.kind) {
                if ramp.kind == .straightRamp { resolveStraightRamp(object, ramp, dt: dt) } else { resolveArcRamp(object, ramp, dt: dt) }
            }
        }
    }

    private struct ArcContact {
        var center: CGPoint
        var radius: CGFloat
        var theta: CGFloat
        var startTheta: CGFloat
        var endTheta: CGFloat
        var surface: CGPoint
        var radial: CGVector
        var normal: CGVector
        var tangent: CGVector
        var angle: CGFloat
        var radialDistance: CGFloat
        var clampSide: Int
        var clamped: Bool
    }

    private func normalizedAngle(_ theta: CGFloat, near start: CGFloat, end: CGFloat) -> CGFloat {
        var value = theta
        if start - end > .pi {
            while value > start { value -= 2 * .pi }
            while value < end { value += 2 * .pi }
        } else {
            while value > start + .pi { value -= 2 * .pi }
            while value < end - .pi { value += 2 * .pi }
        }
        return value
    }

    private func arcContact(ramp: PhysicsObject, point: CGPoint) -> ArcContact? {
        let width = max(0, ramp.size.width)
        let height = max(1, ramp.size.height)
        let sign: CGFloat = ramp.horizontal ? 1 : -1
        let localX = sign * (point.x - ramp.position.x)
        let localY = point.y - ramp.position.y
        let radius: CGFloat
        let centerX: CGFloat
        let centerY: CGFloat
        let start = CGFloat.pi * 0.5
        let end: CGFloat
        if width <= 1 {
            radius = max(20, height * 0.5)
            centerX = 0
            centerY = 0
            end = start - max(.pi, min(2 * .pi, ramp.arcAngle))
        } else {
            radius = (width * width + height * height) / (2 * height)
            centerX = -width * 0.5
            centerY = height * 0.5 - radius
            end = atan2(-height * 0.5 - centerY, width * 0.5 - centerX)
        }
        let deltaX = localX - centerX
        let deltaY = localY - centerY
        let distance = hypot(deltaX, deltaY)
        guard distance >= 0.001 else { return nil }
        var theta = normalizedAngle(atan2(deltaY, deltaX), near: start, end: end)
        var clamped = false
        var side = 0
        let closed = start - end >= 2 * .pi - 0.01
        if !closed {
            if theta > start { theta = start; clamped = true; side = 1 }
            if theta < end { theta = end; clamped = true; side = -1 }
        }
        let surfaceLocal = CGPoint(x: centerX + radius * cos(theta), y: centerY + radius * sin(theta))
        let radialLocal = CGVector(dx: (surfaceLocal.x - centerX) / radius, dy: (surfaceLocal.y - centerY) / radius)
        let radial = CGVector(dx: sign * radialLocal.dx, dy: radialLocal.dy)
        let surface = CGPoint(x: ramp.position.x + sign * surfaceLocal.x, y: ramp.position.y + surfaceLocal.y)
        let tangent = CGVector(dx: sign * radialLocal.dy, dy: -radialLocal.dx)
        return ArcContact(center: CGPoint(x: ramp.position.x + sign * centerX, y: ramp.position.y + centerY), radius: radius, theta: theta, startTheta: start, endTheta: end, surface: surface, radial: radial, normal: radial * -1, tangent: tangent, angle: atan2(tangent.dy, tangent.dx), radialDistance: distance, clampSide: side, clamped: clamped)
    }

    private func arcContact(ramp: PhysicsObject, theta: CGFloat) -> ArcContact {
        let width = max(0, ramp.size.width)
        let height = max(1, ramp.size.height)
        let sign: CGFloat = ramp.horizontal ? 1 : -1
        let radius = width <= 1 ? max(20, height * 0.5) : (width * width + height * height) / (2 * height)
        let centerX: CGFloat = width <= 1 ? 0 : -width * 0.5
        let centerY: CGFloat = width <= 1 ? 0 : height * 0.5 - radius
        let local = CGPoint(x: centerX + radius * cos(theta), y: centerY + radius * sin(theta))
        let radialLocal = CGVector(dx: cos(theta), dy: sin(theta))
        let radial = CGVector(dx: sign * radialLocal.dx, dy: radialLocal.dy)
        let tangent = CGVector(dx: sign * radialLocal.dy, dy: -radialLocal.dx)
        return ArcContact(center: CGPoint(x: ramp.position.x + sign * centerX, y: ramp.position.y + centerY), radius: radius, theta: theta, startTheta: .pi * 0.5, endTheta: .pi * 0.5 - ramp.arcAngle, surface: CGPoint(x: ramp.position.x + sign * local.x, y: ramp.position.y + local.y), radial: radial, normal: radial * -1, tangent: tangent, angle: atan2(tangent.dy, tangent.dx), radialDistance: radius, clampSide: 0, clamped: false)
    }

    private func requiredCircularNormalAcceleration(_ object: PhysicsObject, contact: ArcContact, targetDistance: CGFloat) -> CGFloat {
        let inward = contact.radial * -1
        let tangentialSpeed = object.velocity.dot(contact.tangent)
        let centripetal = tangentialSpeed * tangentialSpeed / max(0.01, targetDistance / pixelsPerMeter)
        let forceInward = object.mass > 0.001 ? object.displayForce.dot(inward) / object.mass : 0
        return centripetal - forceInward
    }

    private func shouldDetachCircularArc(_ object: PhysicsObject, contact: ArcContact, targetDistance: CGFloat) -> Bool {
        if object.velocity.dot(contact.radial) < -0.03 { return true }
        return requiredCircularNormalAcceleration(object, contact: contact, targetDistance: targetDistance) < -0.01
    }

    private func projectArcPosition(_ object: PhysicsObject, ramp: PhysicsObject, contact: ArcContact, targetDistance: CGFloat) {
        let target = contact.center + contact.radial * targetDistance
        let correction = target - object.position
        if ramp.fixed {
            object.position = target
            return
        }
        let totalMass = object.mass + ramp.mass
        object.position = object.position + correction * (ramp.mass / totalMass)
        ramp.position = ramp.position + correction * (-object.mass / totalMass)
        if ramp.contactGround { object.position.y = target.y }
    }

    private func applyArcReaction(_ ramp: PhysicsObject, impulse: CGVector, dt: CGFloat) {
        guard !ramp.fixed else { return }
        ramp.velocity.dx += impulse.dx / ramp.mass
        if !ramp.contactGround {
            ramp.velocity.dy += impulse.dy / ramp.mass
        } else if impulse.dy < 0 {
            let lift = -impulse.dy - ramp.mass * gravity * dt
            if lift > 0 { ramp.velocity.dy -= lift / ramp.mass; ramp.contactGround = false }
        }
    }

    private func applyArcContactVelocity(_ object: PhysicsObject, ramp: PhysicsObject, normal: CGVector, tangent: CGVector, angle: CGFloat, dt: CGFloat) {
        let firstContact = object.rampContact !== ramp
        object.contactRamp = true
        object.rampContact = ramp
        let fullCircle = ramp.size.width <= 1 && ramp.arcAngle >= 2 * .pi - 0.01
        let rampGrounded = ramp.contactGround
        let rampVelocity = ramp.fixed || rampGrounded ? CGVector.zero : ramp.velocity
        let relativeNormal = (object.velocity - rampVelocity).dot(normal)
        var normalImpulse: CGFloat = 0
        if relativeNormal < 0 {
            var restitution = fullCircle && (ramp.fixed || rampGrounded) ? object.restitution : min(object.restitution, ramp.restitution)
            if object.previousContactRamp || (!fullCircle && abs(relativeNormal) < 0.35) { restitution = 0 }
            let rampInverse = ramp.fixed ? 0 : (rampGrounded ? normal.dx * normal.dx / ramp.mass : 1 / ramp.mass)
            let impulse = -(1 + restitution) * relativeNormal / max(0.001, 1 / object.mass + rampInverse)
            normalImpulse = impulse
            object.velocity = object.velocity + normal * (impulse / object.mass)
            applyArcReaction(ramp, impulse: normal * -impulse, dt: dt)
        }
        var tangentSpeed = object.velocity.dot(tangent)
        var normalForce = object.mass * gravity * abs(cos(angle))
        if fullCircle, let forceContact = arcContact(ramp: ramp, point: object.position) {
            let target = forceContact.radius - (object.kind == .ball ? object.radius : object.size.height * 0.5)
            normalForce = object.mass * max(0, requiredCircularNormalAcceleration(object, contact: forceContact, targetDistance: target))
        }
        if firstContact {
            let contactForce = max(normalForce, normalImpulse / max(0.0001, dt))
            object.supportForce = object.supportForce + normal * contactForce
            ramp.reactionForce = ramp.reactionForce - normal * contactForce
        }
        let frictionChange = object.friction * normalForce / max(0.001, object.mass) * dt
        if firstContact, frictionChange > 0, abs(tangentSpeed) > 0 {
            let old = tangentSpeed
            if abs(tangentSpeed) <= frictionChange, abs(gravity * sin(angle)) <= object.friction * normalForce / object.mass { tangentSpeed = 0 } else { tangentSpeed += tangentSpeed > 0 ? -frictionChange : frictionChange }
            let delta = tangentSpeed - old
            object.velocity = object.velocity + tangent * delta
            let frictionForce = tangent * (delta / max(dt, 0.0001) * object.mass)
            object.frictionForce = object.frictionForce + frictionForce
            ramp.frictionForce = ramp.frictionForce - frictionForce
            applyArcReaction(ramp, impulse: tangent * (-object.mass * delta), dt: dt)
        }
        let noExternalWork = object.appliedForce.length < 0.0001 && windForce < 0.0001 && airResistance < 0.0001
        if firstContact, ramp.fixed, object.friction < 0.0001, noExternalWork, object.previousContactRamp || (fullCircle && object.restitution >= 0.999) {
            let targetKinetic = max(0, object.stepMechanicalEnergy + object.mass * gravity * object.position.y / pixelsPerMeter)
            let targetSpeed = sqrt(2 * targetKinetic / object.mass)
            let current = object.velocity.dot(tangent)
            object.velocity = tangent * (current < 0 ? -targetSpeed : targetSpeed)
        }
    }

    private func resolveStraightRamp(_ object: PhysicsObject, _ ramp: PhysicsObject, dt: CGFloat) {
        let left = ramp.position.x - ramp.size.width * 0.5
        let right = ramp.position.x + ramp.size.width * 0.5
        let rampTop = ramp.position.y - ramp.size.height * 0.5
        let rampBottom = ramp.position.y + ramp.size.height * 0.5
        let circular = object.kind == .ball || object.kind == .pulley
        let halfWidth = circular ? object.radius : object.size.width * 0.5
        let halfHeight = circular ? object.radius : object.size.height * 0.5
        let objectLeft = object.position.x - halfWidth
        let objectRight = object.position.x + halfWidth
        let objectTop = object.position.y - halfHeight
        let objectBottom = object.position.y + halfHeight
        let sideImmovable = ramp.fixed
        let hitsRightWall = ramp.horizontal && objectLeft < right && objectRight > right && objectBottom > rampTop && objectTop < rampBottom && (object.previousVelocity.dx < 0 || object.position.x >= right - halfWidth * 0.6)
        if hitsRightWall {
            let overlap = right - objectLeft
            if sideImmovable { object.position.x = right + halfWidth }
            else {
                let totalMass = object.mass + ramp.mass
                object.position.x += overlap * ramp.mass / totalMass
                ramp.position.x -= overlap * object.mass / totalMass
            }
            let relative = object.velocity.dx - (sideImmovable ? 0 : ramp.velocity.dx)
            if relative < 0 {
                let restitution = -relative < 0.35 ? 0 : min(object.restitution, ramp.restitution)
                let impulse = -(1 + restitution) * relative / max(0.001, 1 / object.mass + (sideImmovable ? 0 : 1 / ramp.mass))
                object.velocity.dx += impulse / object.mass
                if !sideImmovable { ramp.velocity.dx -= impulse / ramp.mass }
            }
            return
        }
        let hitsLeftWall = !ramp.horizontal && objectRight > left && objectLeft < left && objectBottom > rampTop && objectTop < rampBottom && (object.previousVelocity.dx > 0 || object.position.x <= left + halfWidth * 0.6)
        if hitsLeftWall {
            let overlap = objectRight - left
            if sideImmovable { object.position.x = left - halfWidth }
            else {
                let totalMass = object.mass + ramp.mass
                object.position.x -= overlap * ramp.mass / totalMass
                ramp.position.x += overlap * object.mass / totalMass
            }
            let relative = object.velocity.dx - (sideImmovable ? 0 : ramp.velocity.dx)
            if relative > 0 {
                let restitution = relative < 0.35 ? 0 : min(object.restitution, ramp.restitution)
                let impulse = (1 + restitution) * relative / max(0.001, 1 / object.mass + (sideImmovable ? 0 : 1 / ramp.mass))
                object.velocity.dx -= impulse / object.mass
                if !sideImmovable { ramp.velocity.dx += impulse / ramp.mass }
            }
            return
        }
        guard object.position.x + halfWidth >= left, object.position.x - halfWidth <= right else { return }
        let progress = max(0, min(1, (object.position.x - left) / max(1, ramp.size.width)))
        let top = ramp.horizontal ? ramp.position.y + ramp.size.height * 0.5 - ramp.size.height * progress : ramp.position.y - ramp.size.height * 0.5 + ramp.size.height * progress
        let bottom = object.position.y + halfHeight
        guard bottom > top - 5, bottom < top + 15 else { return }
        let tangent = CGVector(dx: ramp.horizontal ? ramp.size.width : -ramp.size.width, dy: -ramp.size.height).normalized
        let normal = CGVector(dx: tangent.dy, dy: -tangent.dx)
        let angle = atan2(tangent.dy, tangent.dx)
        let surfaceOffset = circular ? halfHeight : halfHeight / max(0.15, abs(cos(angle)))
        let penetration = object.position.y - (top - surfaceOffset)
        let correction = max(0, penetration) / max(0.15, abs(normal.dy))
        let immovableRamp = ramp.fixed || ramp.contactGround
        if immovableRamp {
            object.position.y = top - surfaceOffset
        } else {
            let totalMass = object.mass + ramp.mass
            object.position = object.position + normal * (correction * ramp.mass / totalMass)
            ramp.position = ramp.position + normal * (-correction * object.mass / totalMass)
        }
        let rampVelocity = immovableRamp ? CGVector.zero : ramp.velocity
        let inward = (object.velocity - rampVelocity).dot(normal)
        var normalImpulse: CGFloat = 0
        if inward < 0 {
            let rampInverse = ramp.fixed ? 0 : (ramp.contactGround ? normal.dx * normal.dx / ramp.mass : 1 / ramp.mass)
            let restitution = -inward > 0.2 ? min(object.restitution, ramp.restitution) : 0
            normalImpulse = -(1 + restitution) * inward / max(0.001, 1 / object.mass + rampInverse)
            object.velocity = object.velocity + normal * (normalImpulse / object.mass)
            applyArcReaction(ramp, impulse: normal * -normalImpulse, dt: dt)
        }
        object.contactRamp = true
        object.rampContact = ramp
        if object.kind != .ball && object.kind != .pulley { object.rotation = angle }
        let normalForce = max(object.mass * gravity * abs(normal.dy), normalImpulse / max(0.0001, dt))
        object.supportForce = object.supportForce + normal * normalForce
        ramp.reactionForce = ramp.reactionForce - normal * normalForce
        let relativeTangent = (object.velocity - rampVelocity).dot(tangent)
        let maxImpulse = object.friction * normalForce * dt
        let frictionImpulse = max(-maxImpulse, min(maxImpulse, -relativeTangent / max(0.001, 1 / object.mass + (ramp.fixed ? 0 : 1 / ramp.mass))))
        object.velocity = object.velocity + tangent * (frictionImpulse / object.mass)
        if !ramp.fixed { ramp.velocity = ramp.velocity + tangent * (-frictionImpulse / ramp.mass) }
        object.frictionForce = object.frictionForce + tangent * (frictionImpulse / max(0.0001, dt))
        ramp.frictionForce = ramp.frictionForce - tangent * (frictionImpulse / max(0.0001, dt))
    }

    private func resolveArcRamp(_ object: PhysicsObject, _ ramp: PhysicsObject, dt: CGFloat) {
        if !ramp.pipeMode, ramp.size.height > ramp.size.width, ramp.size.height > 1, ramp.size.width > 1 {
            let wallX = ramp.position.x + (ramp.horizontal ? ramp.size.width * 0.5 : -ramp.size.width * 0.5)
            let bottomY = ramp.position.y + ramp.size.height * 0.5
            let topY = ramp.position.y - ramp.size.height * 0.5
            let radius = (ramp.size.width * ramp.size.width + ramp.size.height * ramp.size.height) / (2 * ramp.size.height)
            let centerX = ramp.position.x + (ramp.horizontal ? -ramp.size.width * 0.5 : ramp.size.width * 0.5)
            let centerY = ramp.position.y + ramp.size.height * 0.5 - radius
            let square = radius * radius - (wallX - centerX) * (wallX - centerX)
            var crossingY = topY
            if square >= 0 { crossingY = max(topY, min(bottomY, max(centerY + sqrt(square), centerY - sqrt(square)))) }
            let halfWidth = object.size.width * 0.5
            let halfHeight = object.kind == .ball ? object.radius : object.size.height * 0.5
            if object.position.y + halfHeight > crossingY, object.position.y - halfHeight < bottomY {
                let hitsRightWall = ramp.horizontal && object.position.x - halfWidth < wallX && object.position.x + halfWidth > wallX && (object.previousVelocity.dx < 0 || object.position.x >= wallX - halfWidth * 0.6)
                if hitsRightWall {
                    object.position.x = wallX + halfWidth
                    if object.velocity.dx < 0 { object.velocity.dx = -object.velocity.dx * min(object.restitution, ramp.restitution) }
                    return
                }
                let hitsLeftWall = !ramp.horizontal && object.position.x + halfWidth > wallX && object.position.x - halfWidth < wallX && (object.previousVelocity.dx > 0 || object.position.x <= wallX + halfWidth * 0.6)
                if hitsLeftWall {
                    object.position.x = wallX - halfWidth
                    if object.velocity.dx > 0 { object.velocity.dx = -object.velocity.dx * min(object.restitution, ramp.restitution) }
                    return
                }
            }
        }
        guard var contact = arcContact(ramp: ramp, point: object.position) else { return }
        let halfSize = object.kind == .ball ? object.radius : object.size.height * 0.5
        var radialDistance = (object.position - contact.center).length
        let endDistance = (object.position - contact.surface).length
        let tangentAtEnd = object.velocity.dot(contact.tangent)
        if !ramp.pipeMode, contact.clamped {
            if contact.clampSide == 1 && tangentAtEnd < 0 || contact.clampSide == -1 && tangentAtEnd > 0 { return }
        }
        if !ramp.pipeMode, ramp.size.width > 1, ramp.size.height > ramp.size.width, contact.theta < 0 {
            let radiusMeters = max(0.001, contact.radius / pixelsPerMeter)
            let speed = object.speed
            let threshold2 = sqrt(2 * gravity * radiusMeters)
            let threshold5 = sqrt(5 * gravity * radiusMeters)
            if speed < threshold2 {
                contact = arcContact(ramp: ramp, theta: 0)
                radialDistance = contact.radius - halfSize + 1
                let upward = object.velocity.dot(contact.tangent)
                if upward > 0 { object.velocity = object.velocity - contact.tangent * (1.8 * upward) }
            } else if speed < threshold5 {
                if contact.theta < max(contact.endTheta, -0.55) { return }
            } else if contact.theta <= contact.endTheta + 0.04 {
                object.velocity.dy = 0
                return
            }
        }
        if ramp.pipeMode {
            let diameter = max(1, ramp.pipeDiameter * pixelsPerMeter)
            if contact.clamped, endDistance > halfSize + diameter + 20 { return }
            let innerRadius = max(1, contact.radius - diameter)
            let outerTarget = contact.radius - halfSize
            let innerTarget = innerRadius + halfSize
            let previousRadial = (object.previousPosition - contact.center).length
            let capture = max(24, halfSize * 0.9)
            let hitOuter = radialDistance > outerTarget && (radialDistance - outerTarget <= capture || previousRadial <= outerTarget + 4)
            let hitInner = radialDistance < innerTarget && (innerTarget - radialDistance <= capture || previousRadial >= innerTarget - 4)
            let objectPipeSize = object.kind == .ball ? object.size.width : object.size.height
            let fitsPipe = objectPipeSize <= diameter
            let noClearance = innerTarget >= outerTarget
            let insideTooNarrow = noClearance ? (radialDistance >= outerTarget && radialDistance <= innerTarget) : (radialDistance < outerTarget && radialDistance > innerTarget)
            let crossedOuter = radialDistance < outerTarget && previousRadial >= outerTarget - 4
            let crossedInner = radialDistance > innerTarget && previousRadial <= innerTarget + 4
            let resolveOuter = hitOuter || (!fitsPipe && (crossedOuter || insideTooNarrow && previousRadial >= outerTarget))
            let resolveInner = hitInner || (!fitsPipe && (crossedInner || insideTooNarrow))
            if resolveOuter {
                projectArcPosition(object, ramp: ramp, contact: contact, targetDistance: outerTarget)
                if object.kind != .ball { object.rotation = contact.angle }
                applyArcContactVelocity(object, ramp: ramp, normal: contact.radial * -1, tangent: contact.tangent, angle: contact.angle, dt: dt)
            } else if resolveInner {
                projectArcPosition(object, ramp: ramp, contact: contact, targetDistance: innerTarget)
                if object.kind != .ball { object.rotation = contact.angle }
                applyArcContactVelocity(object, ramp: ramp, normal: contact.radial, tangent: contact.tangent, angle: contact.angle, dt: dt)
            }
            return
        }
        let targetDistance = contact.radius - halfSize
        let penetration = radialDistance - targetDistance
        let capture = max(12, halfSize * 0.75)
        if contact.clamped, endDistance > halfSize + 18 { return }
        let circularGuide = ramp.size.width <= 1 && ramp.arcAngle > .pi + 0.01
        var crossedGuide = false
        if circularGuide {
            let previousRadial = (object.previousPosition - contact.center).length
            crossedGuide = previousRadial <= targetDistance + 2 && radialDistance > targetDistance
        }
        var maintains = false
        if circularGuide, object.previousContactRamp {
            if shouldDetachCircularArc(object, contact: contact, targetDistance: targetDistance) { return }
            maintains = abs(penetration) <= max(20, halfSize * 1.2)
        }
        let approaching = penetration >= -2 && penetration <= capture && object.velocity.dot(contact.radial) > 0
        guard circularGuide ? (maintains || crossedGuide || approaching) : (penetration >= -2 && penetration <= capture) else { return }
        if circularGuide, ramp.arcAngle >= 2 * .pi - 0.01, maintains, let previous = arcContact(ramp: ramp, point: object.previousPosition) {
            let relative = object.velocity - (ramp.fixed ? .zero : ramp.velocity)
            contact = arcContact(ramp: ramp, theta: previous.theta - relative.dot(previous.tangent) * pixelsPerMeter * dt / max(1, targetDistance))
            radialDistance = targetDistance
        }
        projectArcPosition(object, ramp: ramp, contact: contact, targetDistance: targetDistance)
        if object.kind != .ball { object.rotation = contact.angle }
        applyArcContactVelocity(object, ramp: ramp, normal: contact.radial * -1, tangent: contact.tangent, angle: contact.angle, dt: dt)
    }

    private func isFullCircularTrack(_ ramp: PhysicsObject) -> Bool { ramp.kind == .arcRamp && ramp.size.width <= 1 && !ramp.pipeMode && ramp.arcAngle >= 2 * .pi - 0.01 }

    private func prepareCircularHorizontalConservation() {
        let horizontalWind = windForce * cos(windDirection * .pi / 180)
        for ramp in objects where ramp.kind == .arcRamp {
            ramp.conserveCircleHorizontal = false
            guard isFullCircularTrack(ramp), !ramp.fixed, abs(horizontalWind) <= 0.0001, airResistance <= 0.0001 else { ramp.circleMomentumInitialized = false; ramp.circleSystemMemberIDs.removeAll(); continue }
            let radius = max(20, ramp.size.height * 0.5)
            let members = objects.filter { candidate in
                candidate === ramp || (!candidate.fixed && ![.spring, .rope, .rod, .straightRamp, .arcRamp].contains(candidate.kind) && (candidate.position - ramp.position).length <= radius + 2)
            }
            let totalMass = members.reduce(0) { $0 + $1.mass }
            let external = members.contains { abs($0.appliedForce.dx) > 0.0001 || $0.contactLeftWall || $0.contactRightWall }
            guard !external, members.count > 1, totalMass > 0 else { ramp.circleMomentumInitialized = false; ramp.circleSystemMemberIDs.removeAll(); continue }
            let ids = members.map(\.id)
            if !ramp.circleMomentumInitialized || ramp.circleSystemMemberIDs != ids {
                ramp.circleSystemMemberIDs = ids
                ramp.circleSystemMass = totalMass
                ramp.circleSystemCenterX = members.reduce(0) { $0 + $1.mass * $1.position.x } / totalMass
                ramp.circleSystemMomentumX = members.reduce(0) { $0 + $1.mass * $1.velocity.dx }
                ramp.circleMomentumInitialized = true
            }
            ramp.conserveCircleHorizontal = true
        }
    }

    private func enforceCircularHorizontalConservation(_ dt: CGFloat) {
        for ramp in objects where ramp.conserveCircleHorizontal && ramp.circleMomentumInitialized && isFullCircularTrack(ramp) {
            let members = ramp.circleSystemMemberIDs.compactMap { id in objects.first { $0.id == id } }
            let totalMass = members.reduce(0) { $0 + $1.mass }
            guard members.count == ramp.circleSystemMemberIDs.count, !members.contains(where: { $0.contactLeftWall || $0.contactRightWall }), abs(totalMass - ramp.circleSystemMass) <= 0.001 else { ramp.circleMomentumInitialized = false; ramp.conserveCircleHorizontal = false; continue }
            let weightedX = members.reduce(0) { $0 + $1.mass * $1.position.x }
            let momentum = members.reduce(0) { $0 + $1.mass * $1.velocity.dx }
            let expectedCenter = ramp.circleSystemCenterX + ramp.circleSystemMomentumX / totalMass * dt * pixelsPerMeter
            let positionCorrection = expectedCenter - weightedX / totalMass
            let velocityCorrection = (ramp.circleSystemMomentumX - momentum) / totalMass
            for member in members { member.position.x += positionCorrection; member.velocity.dx += velocityCorrection }
            ramp.circleSystemCenterX = expectedCenter
        }
    }

    private func solveContainers(_ dt: CGFloat) {
        for container in objects where container.kind == .container {
            let wall = max(2, container.wallThickness * pixelsPerMeter)
            let inner = container.bounds.insetBy(dx: wall, dy: wall)
            for object in objects where object !== container && !object.fixed && ![.spring, .rope, .rod, .ring, .straightRamp, .arcRamp, .container].contains(object.kind) {
                let halfWidth = object.kind == .ball ? object.radius : object.size.width * 0.5
                let halfHeight = object.kind == .ball ? object.radius : object.size.height * 0.5
                guard object.position.x + halfWidth >= container.bounds.minX, object.position.x - halfWidth <= container.bounds.maxX, object.position.y + halfHeight >= container.bounds.minY, object.position.y - halfHeight <= container.bounds.maxY else { continue }
                let inside = object.position.x > inner.minX && object.position.x < inner.maxX && object.position.y > container.bounds.minY
                let waterTop = inner.maxY - container.waterLevel * pixelsPerMeter
                let inWater = container.waterEnabled && object.position.y > waterTop && object.position.y < inner.maxY
                if inside {
                    if object.position.x - halfWidth < inner.minX { object.position.x = inner.minX + halfWidth; if object.velocity.dx < 0 { object.velocity.dx = -object.velocity.dx * (inWater ? min(0.05, object.restitution * 0.15) : object.restitution) } }
                    if object.position.x + halfWidth > inner.maxX { object.position.x = inner.maxX - halfWidth; if object.velocity.dx > 0 { object.velocity.dx = -object.velocity.dx * (inWater ? min(0.05, object.restitution * 0.15) : object.restitution) } }
                    if object.position.y + halfHeight > inner.maxY { object.position.y = inner.maxY - halfHeight; object.contactGround = true; if object.velocity.dy > 0 { if inWater { object.velocity.dy = 0; object.velocity.dx *= 0.25 } else { object.velocity.dy = -object.velocity.dy * object.restitution } } }
                } else {
                    if object.position.y + halfHeight <= container.bounds.minY { continue }
                    if object.position.x + halfWidth > container.bounds.minX, object.position.x - halfWidth < container.bounds.minX { object.position.x = container.bounds.minX - halfWidth; if object.velocity.dx > 0 { object.velocity.dx = -object.velocity.dx * object.restitution } }
                    else if object.position.x - halfWidth < container.bounds.maxX, object.position.x + halfWidth > container.bounds.maxX { object.position.x = container.bounds.maxX + halfWidth; if object.velocity.dx < 0 { object.velocity.dx = -object.velocity.dx * object.restitution } }
                    if object.position.y - halfHeight < container.bounds.maxY, object.position.y + halfHeight > container.bounds.maxY { object.position.y = container.bounds.maxY + halfHeight; if object.velocity.dy < 0 { object.velocity.dy = -object.velocity.dy * object.restitution } }
                }
                if container.waterEnabled {
                    let halfSize = object.kind == .ball ? object.radius : object.size.height * 0.5
                    let objectBottom = object.position.y + halfSize
                    let objectTop = object.position.y - halfSize
                    if object.position.x > inner.minX, object.position.x < inner.maxX, objectBottom > waterTop, objectTop < inner.maxY {
                        let submergedHeight = min(objectBottom - waterTop, 2 * halfSize)
                        let submergedRatio = submergedHeight / max(1, 2 * halfSize)
                        var volume: CGFloat
                        if object.kind == .ball { volume = 4 / 3 * .pi * pow(object.realRadius, 3) }
                        else { volume = object.density > 0.1 ? object.mass / object.density : object.baseArea * object.realHeight }
                        if volume < 0.00001 { volume = object.mass / 500 }
                        var buoyancy = volume * submergedRatio * 1000 * gravity
                        let densityRatio = object.mass / (1000 * volume)
                        if densityRatio < 1 { buoyancy = min(buoyancy, object.mass * gravity * 1.35) }
                        object.velocity.dy -= buoyancy / max(0.001, object.mass) * dt
                        let halfMeters = halfSize / pixelsPerMeter
                        let effectiveStiffness = 1000 * gravity * volume / max(0.001, 2 * halfMeters)
                        let criticalDamping = 2 * sqrt(effectiveStiffness * object.mass) * submergedRatio
                        object.velocity.dy *= 1 / (1 + criticalDamping / object.mass * dt)
                        object.velocity.dx *= 1 / (1 + criticalDamping * 0.5 / object.mass * dt)
                        if densityRatio < 1 {
                            let targetRatio = max(0.02, min(0.98, densityRatio))
                            let equilibriumY = waterTop + targetRatio * 2 * halfSize - halfSize
                            if object.position.y <= equilibriumY, object.velocity.dy < 0 { object.position.y = equilibriumY; object.velocity.dy = 0 }
                            else if object.position.y > equilibriumY, object.velocity.dy < 0 { object.velocity.dy = max(object.velocity.dy, -(object.position.y - equilibriumY) / max(0.001, dt) / pixelsPerMeter) }
                        }
                        object.pressure = 1000 * gravity * (objectBottom - waterTop) / pixelsPerMeter
                    } else { object.pressure = 0 }
                }
            }
        }
    }

    private func recordMotion(_ dt: CGFloat, startingVelocities: [UUID: CGVector]) {
        for object in objects {
            if dt > 0, !isConstraintBody(object), let startingVelocity = startingVelocities[object.id] { object.acceleration = (object.velocity - startingVelocity) / dt }
            if object.showTrajectory {
                object.trajectory.append(object.position)
                if object.trajectory.count > 500 { object.trajectory.removeFirst() }
            }
            if object.showState {
                let displacement = hypot(object.position.x - object.initialPosition.x, object.position.y - object.initialPosition.y) / pixelsPerMeter
                object.velocityHistory.append(object.speed)
                object.positionHistory.append(displacement)
                object.accelerationHistory.append(object.acceleration.length)
                if object.velocityHistory.count > 100 {
                    object.velocityHistory.removeFirst()
                    object.positionHistory.removeFirst()
                    object.accelerationHistory.removeFirst()
                }
            }
        }
    }

    func hitTest(_ point: CGPoint, prioritizeRods: Bool = true) -> PhysicsObject? {
        if prioritizeRods {
            for object in objects.reversed() where object.kind == .rod && !object.isCutHalf {
                let local = rotate(point - object.position, by: -object.rotation)
                if abs(local.dx) <= object.size.width * 0.5 + 8, abs(local.dy) <= 22 { return object }
                let ends = endpoints(of: object)
                if distanceToSegment(point, ends.0, ends.1) <= 22 { return object }
            }
        }
        for object in objects.reversed() {
            if object.isCutHalf { continue }
            switch object.kind {
            case .ball:
                if object.polygon.isEmpty {
                    if (point - object.position).length <= object.radius { return object }
                } else {
                    let local = rotate(point - object.position, by: -object.rotation)
                    if abs(local.dx) <= object.size.width * 0.5, abs(local.dy) <= object.size.height * 0.5 { return object }
                }
            case .spring, .rope:
                if object.kind == .rope, object.ropeWrapped, object.wrapPoints.count > 1 {
                    for index in 0..<(object.wrapPoints.count - 1) {
                        if distanceToSegment(point, object.wrapPoints[index], object.wrapPoints[index + 1]) <= 15 { return object }
                    }
                } else {
                    let ends = endpoints(of: object)
                    if distanceToSegment(point, ends.0, ends.1) <= (object.kind == .spring ? 22 : 15) { return object }
                }
            case .rod:
                let ends = endpoints(of: object)
                if distanceToSegment(point, ends.0, ends.1) <= 15 { return object }
            case .ring:
                if (point - object.position).length <= object.radius + 8 { return object }
            case .pulley:
                if (point - object.position).length <= object.radius { return object }
            case .straightRamp:
                let local = rotate(point - object.position, by: -object.rotation)
                let halfWidth = object.size.width * 0.5
                let halfHeight = object.size.height * 0.5
                let first = CGPoint(x: object.horizontal ? -halfWidth : halfWidth, y: halfHeight)
                let second = CGPoint(x: object.horizontal ? halfWidth : -halfWidth, y: halfHeight)
                let third = CGPoint(x: object.horizontal ? halfWidth : -halfWidth, y: -halfHeight)
                let d1 = (local.dx - first.x) * (second.y - first.y) - (second.x - first.x) * (local.dy - first.y)
                let d2 = (local.dx - second.x) * (third.y - second.y) - (third.x - second.x) * (local.dy - second.y)
                let d3 = (local.dx - third.x) * (first.y - third.y) - (first.x - third.x) * (local.dy - third.y)
                let hasNegative = d1 < 0 || d2 < 0 || d3 < 0
                let hasPositive = d1 > 0 || d2 > 0 || d3 > 0
                if !(hasNegative && hasPositive) { return object }
            case .arcRamp:
                if let contact = arcContact(ramp: object, point: point) {
                    let radialDistance = (point - contact.center).length
                    let ppm = object.realWidth > 0.001 ? object.size.width / object.realWidth : (object.realHeight > 0.001 ? object.size.height / object.realHeight : pixelsPerMeter)
                    let tolerance = max(14, object.pipeMode ? object.pipeDiameter * ppm * 0.5 : 14)
                    if !contact.clamped, abs(radialDistance - contact.radius) <= tolerance { return object }
                    if object.pipeMode {
                        let innerRadius = max(1, contact.radius - object.pipeDiameter * ppm)
                        if !contact.clamped, abs(radialDistance - innerRadius) <= tolerance { return object }
                    }
                    if object.size.height > object.size.width, object.size.height > 1, object.size.width > 1 {
                        let wallX = object.horizontal ? object.position.x + object.size.width * 0.5 : object.position.x - object.size.width * 0.5
                        let bottomY = object.position.y + object.size.height * 0.5
                        let topY = object.position.y - object.size.height * 0.5
                        let width = max(0, object.size.width)
                        let radius = (width * width + object.size.height * object.size.height) / (2 * object.size.height)
                        let centerX = object.horizontal ? object.position.x - width * 0.5 : object.position.x + width * 0.5
                        let centerY = object.position.y + object.size.height * 0.5 - radius
                        let deltaX = wallX - centerX
                        var crossY = topY
                        let discriminant = radius * radius - deltaX * deltaX
                        if discriminant >= 0 {
                            crossY = max(centerY + sqrt(discriminant), centerY - sqrt(discriminant))
                            crossY = max(topY, min(bottomY, crossY))
                        }
                        if abs(point.x - wallX) <= 14, point.y >= crossY, point.y <= bottomY { return object }
                    }
                }
            default:
                let local = rotate(point - object.position, by: -object.rotation)
                if abs(local.dx) <= object.size.width * 0.5, abs(local.dy) <= object.size.height * 0.5 { return object }
            }
        }
        return nil
    }

    func isInContact(_ a: PhysicsObject, _ b: PhysicsObject) -> Bool {
        a.bounds.insetBy(dx: -6, dy: -6).intersects(b.bounds.insetBy(dx: -6, dy: -6))
    }

    func addCeiling(from start: CGPoint, to end: CGPoint) {
        ceilings.append(CeilingSegment(start: start, end: CGPoint(x: end.x, y: start.y)))
    }

    func erase(at point: CGPoint) {
        if let index = ceilingIndex(at: point) {
            for object in objects {
                if case .ceiling(let value, _) = object.anchorA, value == index { setAnchor(.none, on: object, endA: true) }
                if case .ceiling(let value, _) = object.anchorB, value == index { setAnchor(.none, on: object, endA: false) }
            }
            ceilings.remove(at: index)
            for object in objects {
                if case .ceiling(let value, let x) = object.anchorA, value > index { object.anchorA = .ceiling(value - 1, x) }
                if case .ceiling(let value, let x) = object.anchorB, value > index { object.anchorB = .ceiling(value - 1, x) }
            }
            return
        }
        if abs(point.y - groundY) < 24 {
            groundEraseRanges.append((point.x - 22)...(point.x + 22))
            mergeGroundRanges()
        }
    }

    private func mergeGroundRanges() {
        let sorted = groundEraseRanges.sorted { $0.lowerBound < $1.lowerBound }
        var merged: [ClosedRange<CGFloat>] = []
        for range in sorted {
            if let last = merged.last, range.lowerBound <= last.upperBound {
                merged[merged.count - 1] = last.lowerBound...max(last.upperBound, range.upperBound)
            } else { merged.append(range) }
        }
        groundEraseRanges = merged
    }

    private func isGroundErased(at x: CGFloat) -> Bool { groundEraseRanges.contains { $0.contains(x) } }

    func cut(from start: CGPoint, to end: CGPoint) {
        for object in Array(objects.reversed()) {
            if object.isCutHalf { continue }
            if object.kind == .rope || object.kind == .rod {
                let points = endpoints(of: object)
                if segmentsIntersect(start, end, points.0, points.1) { remove(object) }
                continue
            }
            if object.kind == .spring {
                let points = endpoints(of: object)
                guard segmentsIntersect(start, end, points.0, points.1) else { continue }
                createCutSpringHalves(object, endpoints: points, cutStart: start, cutEnd: end)
                remove(object)
                continue
            }
            guard ![.straightRamp, .arcRamp, .container, .pulley, .ring].contains(object.kind), lineIntersects(object, start: start, end: end) else { continue }
            clip(object, start: start, end: end)
            break
        }
    }

    private func createCutSpringHalves(_ spring: PhysicsObject, endpoints: (CGPoint, CGPoint), cutStart: CGPoint, cutEnd: CGPoint) {
        guard let intersection = segmentIntersectionParameters(endpoints.0, endpoints.1, cutStart, cutEnd) else { return }
        let parameter = max(0.1, min(0.9, intersection.0))
        let cutPoint = endpoints.0 + (endpoints.1 - endpoints.0) * parameter
        let originalLength = (endpoints.1 - endpoints.0).length
        let tension = originalLength > 0.001 ? spring.springConstant * (originalLength - spring.naturalLength) / pixelsPerMeter * 0.1 : 0
        let values: [(CGPoint, Anchor)] = [(endpoints.0, spring.anchorA), (endpoints.1, spring.anchorB)]
        for value in values {
            let length = (cutPoint - value.0).length
            guard length > 5 else { continue }
            let half = PhysicsObject(kind: .spring, position: CGPoint(x: (value.0.x + cutPoint.x) * 0.5, y: (value.0.y + cutPoint.y) * 0.5), size: CGSize(width: length, height: 20))
            half.isCutHalf = true
            half.cutHalfAnchor = value.0
            half.cutHalfFree = cutPoint
            half.cutHalfLength = length
            half.deformation = tension
            if case .object(let id) = value.1 { half.cutHalfFollowObject = objects.first { $0.id == id && !$0.fixed } }
            half.rotation = atan2(cutPoint.y - value.0.y, cutPoint.x - value.0.x)
            objects.append(half)
        }
    }

    func findCutTarget(from start: CGPoint, to end: CGPoint) -> PhysicsObject? {
        objects.reversed().first {
            ![.spring, .rope, .ring, .straightRamp, .arcRamp, .container, .pulley].contains($0.kind) && lineIntersects($0, start: start, end: end)
        }
    }

    func polygonForFineCut(_ object: PhysicsObject) -> [CGPoint] {
        if !object.polygon.isEmpty { return object.polygon }
        if object.kind == .ball {
            return (0..<48).map {
                let angle = CGFloat($0) * 2 * .pi / 48
                return CGPoint(x: object.radius * cos(angle), y: object.radius * sin(angle))
            }
        }
        let halfWidth = object.size.width * 0.5
        let halfHeight = object.size.height * 0.5
        return [CGPoint(x: -halfWidth, y: -halfHeight), CGPoint(x: halfWidth, y: -halfHeight), CGPoint(x: halfWidth, y: halfHeight), CGPoint(x: -halfWidth, y: halfHeight)]
    }

    func splitPolygon(_ polygon: [CGPoint], by chain: [CGPoint]) -> ([CGPoint], [CGPoint])? {
        guard polygon.count >= 3, chain.count >= 2 else { return nil }
        var enterSegment = -1
        var exitSegment = -1
        var enterEdge = -1
        var exitEdge = -1
        var enterT: CGFloat = 0
        var exitT: CGFloat = 0
        for chainIndex in 0..<(chain.count - 1) {
            var best = CGFloat.greatestFiniteMagnitude
            for edgeIndex in polygon.indices {
                if let values = segmentIntersectionParameters(chain[chainIndex], chain[chainIndex + 1], polygon[edgeIndex], polygon[(edgeIndex + 1) % polygon.count]), values.0 < best {
                    best = values.0
                    enterSegment = chainIndex
                    enterEdge = edgeIndex
                    enterT = values.0
                }
            }
            if enterSegment >= 0 { break }
        }
        guard enterSegment >= 0 else { return nil }
        for chainIndex in stride(from: chain.count - 2, through: 0, by: -1) {
            var best = -CGFloat.greatestFiniteMagnitude
            for edgeIndex in polygon.indices {
                if let values = segmentIntersectionParameters(chain[chainIndex], chain[chainIndex + 1], polygon[edgeIndex], polygon[(edgeIndex + 1) % polygon.count]), values.0 > best {
                    best = values.0
                    exitSegment = chainIndex
                    exitEdge = edgeIndex
                    exitT = values.0
                }
            }
            if exitSegment >= 0 { break }
        }
        guard exitSegment >= 0, !(enterSegment == exitSegment && abs(enterT - exitT) < 0.0001), enterSegment <= exitSegment else { return nil }
        let enterDelta = chain[enterSegment + 1] - chain[enterSegment]
        let exitDelta = chain[exitSegment + 1] - chain[exitSegment]
        let enterPoint = chain[enterSegment] + enterDelta * enterT
        let exitPoint = chain[exitSegment] + exitDelta * exitT
        var inside = [enterPoint]
        if enterSegment + 1 <= exitSegment {
            for index in (enterSegment + 1)...exitSegment { inside.append(chain[index]) }
        }
        inside.append(exitPoint)
        var first = inside
        var vertexIndex = (exitEdge + 1) % polygon.count
        while true {
            first.append(polygon[vertexIndex])
            if vertexIndex == enterEdge { break }
            vertexIndex = (vertexIndex + 1) % polygon.count
            if first.count > polygon.count + inside.count + 2 { return nil }
        }
        var second = Array(inside.reversed())
        vertexIndex = (enterEdge + 1) % polygon.count
        while true {
            second.append(polygon[vertexIndex])
            if vertexIndex == exitEdge { break }
            vertexIndex = (vertexIndex + 1) % polygon.count
            if second.count > polygon.count + inside.count + 2 { return nil }
        }
        guard abs(polygonArea(first)) >= 4, abs(polygonArea(second)) >= 4 else { return nil }
        return (first, second)
    }

    func applyFineCut(_ kept: [CGPoint], original: [CGPoint], to object: PhysicsObject) {
        guard kept.count >= 3 else { return }
        let originalArea = abs(polygonArea(original))
        let keptArea = abs(polygonArea(kept))
        if originalArea > 0.001 { object.mass *= keptArea / originalArea }
        let center = polygonCentroid(kept)
        object.polygon = kept.map { CGPoint(x: $0.x - center.x, y: $0.y - center.y) }
        object.position = localToWorld(center, object: object)
        let xValues = object.polygon.map(\.x)
        let yValues = object.polygon.map(\.y)
        object.size = CGSize(width: (xValues.max() ?? 0) - (xValues.min() ?? 0), height: (yValues.max() ?? 0) - (yValues.min() ?? 0))
        if object.kind == .ball { object.realRadius = max(object.size.width, object.size.height) * 0.5 / pixelsPerMeter }
    }

    private func clip(_ object: PhysicsObject, start: CGPoint, end: CGPoint) {
        let localStart = worldToLocal(start, object: object)
        let localEnd = worldToLocal(end, object: object)
        var vertices = object.polygon
        if vertices.isEmpty {
            if object.kind == .ball {
                vertices = (0..<64).map { index in
                    let angle = CGFloat(index) * 2 * .pi / 64
                    return CGPoint(x: cos(angle) * object.radius, y: sin(angle) * object.radius)
                }
            } else {
                let width = object.size.width * 0.5
                let height = object.size.height * 0.5
                vertices = [CGPoint(x: -width, y: -height), CGPoint(x: width, y: -height), CGPoint(x: width, y: height), CGPoint(x: -width, y: height)]
            }
        }
        let line = localEnd - localStart
        let normal = CGVector(dx: line.dy, dy: -line.dx).normalized
        var clipped: [CGPoint] = []
        for index in vertices.indices {
            let current = vertices[index]
            let next = vertices[(index + 1) % vertices.count]
            let d1 = (current - localStart).dot(normal)
            let d2 = (next - localStart).dot(normal)
            if d1 >= 0 { clipped.append(current) }
            if (d1 >= 0) != (d2 >= 0) {
                let t = d1 / (d1 - d2)
                clipped.append(CGPoint(x: current.x + (next.x - current.x) * t, y: current.y + (next.y - current.y) * t))
            }
        }
        guard clipped.count >= 3 else { return }
        let oldArea = abs(polygonArea(vertices))
        let newArea = abs(polygonArea(clipped))
        guard oldArea > 0, newArea / oldArea >= 0.05, newArea / oldArea <= 0.95 else { return }
        let centroid = polygonCentroid(clipped)
        object.polygon = clipped.map { CGPoint(x: $0.x - centroid.x, y: $0.y - centroid.y) }
        object.position = localToWorld(centroid, object: object)
        object.mass *= newArea / oldArea
        let xs = object.polygon.map(\.x)
        let ys = object.polygon.map(\.y)
        object.size = CGSize(width: (xs.max() ?? 0) - (xs.min() ?? 0), height: (ys.max() ?? 0) - (ys.min() ?? 0))
    }

    func cutPercentage(object: PhysicsObject, from start: CGPoint, to end: CGPoint) -> CGFloat {
        let localStart = worldToLocal(start, object: object)
        let localEnd = worldToLocal(end, object: object)
        let vertices = polygonForFineCut(object)
        let line = localEnd - localStart
        let normal = CGVector(dx: line.dy, dy: -line.dx).normalized
        var clipped: [CGPoint] = []
        for index in vertices.indices {
            let a = vertices[index]
            let b = vertices[(index + 1) % vertices.count]
            let d1 = (a - localStart).dot(normal)
            let d2 = (b - localStart).dot(normal)
            if d1 >= 0 { clipped.append(a) }
            if (d1 >= 0) != (d2 >= 0) {
                let t = d1 / (d1 - d2)
                clipped.append(CGPoint(x: a.x + (b.x - a.x) * t, y: a.y + (b.y - a.y) * t))
            }
        }
        let original = abs(polygonArea(vertices))
        guard original > 0 else { return 0 }
        let fraction = abs(polygonArea(clipped)) / original
        return min(fraction, 1 - fraction) * 100
    }

    private func worldToLocal(_ point: CGPoint, object: PhysicsObject) -> CGPoint {
        let delta = point - object.position
        return CGPoint(x: delta.dx * cos(object.rotation) + delta.dy * sin(object.rotation), y: -delta.dx * sin(object.rotation) + delta.dy * cos(object.rotation))
    }

    private func localToWorld(_ point: CGPoint, object: PhysicsObject) -> CGPoint {
        CGPoint(x: object.position.x + point.x * cos(object.rotation) - point.y * sin(object.rotation), y: object.position.y + point.x * sin(object.rotation) + point.y * cos(object.rotation))
    }

    private func lineIntersects(_ object: PhysicsObject, start: CGPoint, end: CGPoint) -> Bool {
        if object.kind == .ball, object.polygon.isEmpty { return distanceToSegment(object.position, start, end) <= object.radius }
        let a = worldToLocal(start, object: object)
        let b = worldToLocal(end, object: object)
        let width = object.size.width * 0.5
        let height = object.size.height * 0.5
        let vertices = object.polygon.isEmpty ? [CGPoint(x: -width, y: -height), CGPoint(x: width, y: -height), CGPoint(x: width, y: height), CGPoint(x: -width, y: height)] : object.polygon
        return vertices.indices.contains { segmentsIntersect(a, b, vertices[$0], vertices[($0 + 1) % vertices.count]) }
    }

    private func polygonArea(_ vertices: [CGPoint]) -> CGFloat {
        guard vertices.count > 2 else { return 0 }
        return vertices.indices.reduce(0) { result, index in
            let a = vertices[index]
            let b = vertices[(index + 1) % vertices.count]
            return result + a.x * b.y - b.x * a.y
        } * 0.5
    }

    private func polygonCentroid(_ vertices: [CGPoint]) -> CGPoint {
        let area = polygonArea(vertices)
        guard abs(area) > 0.0001 else { return .zero }
        var x: CGFloat = 0
        var y: CGFloat = 0
        for index in vertices.indices {
            let a = vertices[index]
            let b = vertices[(index + 1) % vertices.count]
            let cross = a.x * b.y - b.x * a.y
            x += (a.x + b.x) * cross
            y += (a.y + b.y) * cross
        }
        return CGPoint(x: x / (6 * area), y: y / (6 * area))
    }

    func distanceToSegment(_ point: CGPoint, _ start: CGPoint, _ end: CGPoint) -> CGFloat {
        let line = end - start
        let lengthSquared = line.dx * line.dx + line.dy * line.dy
        let t = lengthSquared > 0 ? max(0, min(1, ((point.x - start.x) * line.dx + (point.y - start.y) * line.dy) / lengthSquared)) : 0
        return hypot(point.x - start.x - line.dx * t, point.y - start.y - line.dy * t)
    }

    private func segmentsIntersect(_ a: CGPoint, _ b: CGPoint, _ c: CGPoint, _ d: CGPoint) -> Bool {
        let first = b - a
        let second = d - c
        let cross = first.dx * second.dy - first.dy * second.dx
        guard abs(cross) > 0.0001 else { return false }
        let offset = c - a
        let t = (offset.dx * second.dy - offset.dy * second.dx) / cross
        let u = (offset.dx * first.dy - offset.dy * first.dx) / cross
        return (0...1).contains(t) && (0...1).contains(u)
    }

    private func segmentIntersectionParameters(_ a: CGPoint, _ b: CGPoint, _ c: CGPoint, _ d: CGPoint) -> (CGFloat, CGFloat)? {
        let first = b - a
        let second = d - c
        let cross = first.dx * second.dy - first.dy * second.dx
        guard abs(cross) >= 0.000001 else { return nil }
        let offset = c - a
        let t = (offset.dx * second.dy - offset.dy * second.dx) / cross
        let u = (offset.dx * first.dy - offset.dy * first.dx) / cross
        guard (0...1).contains(t), (0...1).contains(u) else { return nil }
        return (t, u)
    }
}
