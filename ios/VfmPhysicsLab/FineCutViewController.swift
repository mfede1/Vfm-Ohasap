import UIKit

final class FineCutViewController: UIViewController {
    let object: PhysicsObject
    let engine: PhysicsEngine
    let canvas: FineCutCanvas
    var onApply: (() -> Void)?

    init(object: PhysicsObject, engine: PhysicsEngine) {
        self.object = object
        self.engine = engine
        self.canvas = FineCutCanvas(polygon: engine.polygonForFineCut(object), pixelsPerMeter: engine.pixelsPerMeter, decimalPlaces: engine.decimalPlaces)
        super.init(nibName: nil, bundle: nil)
        title = "精细切割"
        preferredContentSize = CGSize(width: 760, height: 540)
    }

    required init?(coder: NSCoder) { nil }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        canvas.splitter = { [weak engine] polygon, chain in engine?.splitPolygon(polygon, by: chain) }
        let undo = UIButton(type: .system)
        undo.setTitle("撤销点", for: .normal)
        undo.addTarget(self, action: #selector(undoPoint), for: .touchUpInside)
        let swap = UIButton(type: .system)
        swap.setTitle("切换保留部分", for: .normal)
        swap.addTarget(self, action: #selector(swapPiece), for: .touchUpInside)
        let row = UIStackView(arrangedSubviews: [undo, swap])
        row.axis = .horizontal
        row.distribution = .fillEqually
        row.spacing = 8
        canvas.translatesAutoresizingMaskIntoConstraints = false
        row.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(canvas)
        view.addSubview(row)
        NSLayoutConstraint.activate([
            canvas.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 12),
            canvas.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -12),
            canvas.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 8),
            canvas.bottomAnchor.constraint(equalTo: row.topAnchor, constant: -8),
            row.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 12),
            row.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -12),
            row.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -8),
            row.heightAnchor.constraint(equalToConstant: 42)
        ])
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: "取消", style: .plain, target: self, action: #selector(cancel))
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "确认切割", style: .done, target: self, action: #selector(confirm))
    }

    @objc private func undoPoint() {
        if !canvas.cutPoints.isEmpty { canvas.cutPoints.removeLast(); canvas.recompute(); canvas.setNeedsDisplay() }
    }

    @objc private func swapPiece() { canvas.keepFirst.toggle(); canvas.setNeedsDisplay() }
    @objc private func cancel() { dismiss(animated: true) }

    @objc private func confirm() {
        guard let kept = canvas.keptPiece else { return }
        engine.applyFineCut(kept, original: canvas.polygon, to: object)
        onApply?()
        dismiss(animated: true)
    }
}

final class FineCutCanvas: UIView {
    let polygon: [CGPoint]
    let pixelsPerMeter: CGFloat
    let decimalPlaces: Int
    var cutPoints: [CGPoint] = []
    var keepFirst = true
    var pieces: ([CGPoint], [CGPoint])?
    var splitter: (([CGPoint], [CGPoint]) -> ([CGPoint], [CGPoint])?)?
    private var scale: CGFloat = 1
    private var offset = CGPoint.zero

    init(polygon: [CGPoint], pixelsPerMeter: CGFloat, decimalPlaces: Int) {
        self.polygon = polygon
        self.pixelsPerMeter = pixelsPerMeter
        self.decimalPlaces = decimalPlaces
        super.init(frame: .zero)
        backgroundColor = .white
    }

    required init?(coder: NSCoder) { nil }
    var keptPiece: [CGPoint]? { guard let pieces else { return nil }; return keepFirst ? pieces.0 : pieces.1 }
    func recompute() { pieces = cutPoints.count >= 2 ? splitter?(polygon, cutPoints) : nil }
    private func screen(_ point: CGPoint) -> CGPoint { CGPoint(x: point.x * scale + offset.x, y: point.y * scale + offset.y) }
    private func local(_ point: CGPoint) -> CGPoint { CGPoint(x: (point.x - offset.x) / scale, y: (point.y - offset.y) / scale) }

    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        guard let point = touches.first?.location(in: self) else { return }
        cutPoints.append(local(point))
        recompute()
        setNeedsDisplay()
    }

    override func draw(_ rect: CGRect) {
        guard let context = UIGraphicsGetCurrentContext(), !polygon.isEmpty else { return }
        let minX = polygon.map(\.x).min() ?? 0
        let maxX = polygon.map(\.x).max() ?? 1
        let minY = polygon.map(\.y).min() ?? 0
        let maxY = polygon.map(\.y).max() ?? 1
        scale = min(bounds.width * 0.72 / max(1, maxX - minX), bounds.height * 0.65 / max(1, maxY - minY))
        offset = CGPoint(x: bounds.midX - (minX + maxX) * 0.5 * scale, y: bounds.height * 0.42 - (minY + maxY) * 0.5 * scale)
        context.setStrokeColor(UIColor.systemGray3.cgColor)
        context.setLineWidth(1.5)
        let gridStep = pixelsPerMeter * 0.1 * scale
        if gridStep > 8 {
            context.setStrokeColor(UIColor.black.withAlphaComponent(0.1).cgColor)
            context.setLineWidth(1)
            var gridX = screen(.zero).x.truncatingRemainder(dividingBy: gridStep)
            while gridX < bounds.width { context.move(to: CGPoint(x: gridX, y: 0)); context.addLine(to: CGPoint(x: gridX, y: bounds.height)); gridX += gridStep }
            var gridY = screen(.zero).y.truncatingRemainder(dividingBy: gridStep)
            while gridY < bounds.height { context.move(to: CGPoint(x: 0, y: gridY)); context.addLine(to: CGPoint(x: bounds.width, y: gridY)); gridY += gridStep }
            context.strokePath()
        }
        context.setStrokeColor(UIColor.systemGray3.cgColor)
        context.setLineWidth(1.5)
        context.move(to: CGPoint(x: 0, y: screen(.zero).y))
        context.addLine(to: CGPoint(x: bounds.width, y: screen(.zero).y))
        context.move(to: CGPoint(x: screen(.zero).x, y: 0))
        context.addLine(to: CGPoint(x: screen(.zero).x, y: bounds.height))
        context.strokePath()
        "x".draw(at: CGPoint(x: bounds.width - 18, y: screen(.zero).y - 20), withAttributes: [.font: UIFont.systemFont(ofSize: 14), .foregroundColor: UIColor.systemGray])
        "y".draw(at: CGPoint(x: screen(.zero).x + 6, y: 4), withAttributes: [.font: UIFont.systemFont(ofSize: 14), .foregroundColor: UIColor.systemGray])
        drawPolygon(polygon, fill: UIColor.systemGray.withAlphaComponent(0.18), stroke: .label, width: 2, context: context)
        if let keptPiece {
            drawPolygon(keptPiece, fill: UIColor.systemGreen.withAlphaComponent(0.32), stroke: .systemGreen, width: 2.5, context: context)
            let center = centroid(keptPiece)
            let centerScreen = screen(center)
            context.setStrokeColor(UIColor.systemRed.cgColor)
            context.setLineWidth(2.5)
            context.strokeEllipse(in: CGRect(x: centerScreen.x - 6, y: centerScreen.y - 6, width: 12, height: 12))
            context.move(to: CGPoint(x: centerScreen.x - 12, y: centerScreen.y))
            context.addLine(to: CGPoint(x: centerScreen.x + 12, y: centerScreen.y))
            context.move(to: CGPoint(x: centerScreen.x, y: centerScreen.y - 12))
            context.addLine(to: CGPoint(x: centerScreen.x, y: centerScreen.y + 12))
            context.strokePath()
            let text = "重心(\(SurdFormatter.text(center.x / pixelsPerMeter, places: decimalPlaces)), \(SurdFormatter.text(-center.y / pixelsPerMeter, places: decimalPlaces)))m"
            text.draw(at: CGPoint(x: centerScreen.x + 14, y: centerScreen.y - 18), withAttributes: [.font: UIFont.systemFont(ofSize: 14), .foregroundColor: UIColor.systemRed])
        }
        context.setStrokeColor(UIColor.systemOrange.cgColor)
        context.setFillColor(UIColor.systemOrange.cgColor)
        context.setLineWidth(2.5)
        context.setLineDash(phase: 0, lengths: [10, 5])
        for index in cutPoints.indices {
            let point = screen(cutPoints[index])
            context.fillEllipse(in: CGRect(x: point.x - 5, y: point.y - 5, width: 10, height: 10))
            if index > 0 { context.move(to: screen(cutPoints[index - 1])); context.addLine(to: point) }
        }
        context.strokePath()
        let hint = cutPoints.count < 2 ? "点击取点，连线需从图形外穿过图形" : (keptPiece == nil ? "切割线未有效穿过图形，请继续取点或撤销" : "绿色为保留部分")
        hint.draw(at: CGPoint(x: 10, y: bounds.height - 26), withAttributes: [.font: UIFont.systemFont(ofSize: 14), .foregroundColor: UIColor.systemBlue])
    }

    private func drawPolygon(_ points: [CGPoint], fill: UIColor, stroke: UIColor, width: CGFloat, context: CGContext) {
        guard let first = points.first else { return }
        context.beginPath()
        context.move(to: screen(first))
        points.dropFirst().forEach { context.addLine(to: screen($0)) }
        context.closePath()
        context.setFillColor(fill.cgColor)
        context.setStrokeColor(stroke.cgColor)
        context.setLineWidth(width)
        context.drawPath(using: .fillStroke)
    }

    private func centroid(_ points: [CGPoint]) -> CGPoint {
        var signedArea: CGFloat = 0
        var centerX: CGFloat = 0
        var centerY: CGFloat = 0
        for index in points.indices {
            let first = points[index]
            let second = points[(index + 1) % points.count]
            let cross = first.x * second.y - second.x * first.y
            signedArea += cross
            centerX += (first.x + second.x) * cross
            centerY += (first.y + second.y) * cross
        }
        signedArea *= 0.5
        if abs(signedArea) < 0.001 { return CGPoint(x: points.map(\.x).reduce(0, +) / CGFloat(points.count), y: points.map(\.y).reduce(0, +) / CGFloat(points.count)) }
        return CGPoint(x: centerX / (6 * signedArea), y: centerY / (6 * signedArea))
    }
}
