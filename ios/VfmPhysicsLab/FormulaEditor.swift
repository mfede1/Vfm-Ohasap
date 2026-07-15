import UIKit

enum FormulaNodeKind {
    case character
    case fraction
    case squareRoot
    case nthRoot
    case power
}

final class FormulaNode {
    let kind: FormulaNodeKind
    let character: Character
    weak var parentSequence: FormulaSequence?
    lazy var slots: [FormulaSequence] = {
        let count: Int
        switch kind {
        case .character: count = 0
        case .squareRoot: count = 1
        case .fraction, .nthRoot, .power: count = 2
        }
        return (0..<count).map { FormulaSequence(owner: self, slotIndex: $0) }
    }()

    init(kind: FormulaNodeKind, character: Character = "\0") {
        self.kind = kind
        self.character = character
    }

    static func character(_ value: Character) -> FormulaNode { FormulaNode(kind: .character, character: value) }
    static func fraction() -> FormulaNode { FormulaNode(kind: .fraction) }
    static func squareRoot() -> FormulaNode { FormulaNode(kind: .squareRoot) }
    static func nthRoot() -> FormulaNode { FormulaNode(kind: .nthRoot) }
    static func power() -> FormulaNode { FormulaNode(kind: .power) }
}

final class FormulaSequence {
    weak var owner: FormulaNode?
    let slotIndex: Int
    var nodes: [FormulaNode] = []

    init(owner: FormulaNode?, slotIndex: Int) {
        self.owner = owner
        self.slotIndex = slotIndex
    }

    func insert(_ node: FormulaNode, at index: Int) {
        node.parentSequence = self
        nodes.insert(node, at: max(0, min(nodes.count, index)))
    }
}

final class FormulaTree {
    let root = FormulaSequence(owner: nil, slotIndex: -1)

    static func parse(_ string: String) -> FormulaTree {
        let tree = FormulaTree()
        let characters = Array(string)
        var position = 0
        parseSequence(tree.root, characters, &position)
        return tree
    }

    private static func parseSequence(_ sequence: FormulaSequence, _ characters: [Character], _ position: inout Int) {
        while position < characters.count {
            let character = characters[position]
            if character == ")" || character == "," || character == "}" { break }
            if let function = peekFunction(characters, position) {
                position += function.count + 1
                if let node = buildFunction(function) {
                    parseArguments(node, characters, &position)
                    sequence.insert(node, at: sequence.nodes.count)
                } else {
                    for value in Array(function + "(") { sequence.insert(.character(value), at: sequence.nodes.count) }
                }
                continue
            }
            if character == "^" {
                position += 1
                let node = FormulaNode.power()
                let base = sequence.nodes.popLast() ?? .character("?")
                node.slots[0].insert(base, at: 0)
                if position < characters.count, characters[position] == "{" {
                    position += 1
                    parseSequence(node.slots[1], characters, &position)
                    if position < characters.count, characters[position] == "}" { position += 1 }
                } else {
                    node.slots[1].insert(.character(position < characters.count ? characters[position] : "?"), at: 0)
                    position = min(characters.count, position + 1)
                }
                sequence.insert(node, at: sequence.nodes.count)
                continue
            }
            if character == "/" {
                position += 1
                let node = FormulaNode.fraction()
                node.slots[0].insert(sequence.nodes.popLast() ?? .character("?"), at: 0)
                while position < characters.count, !["+", "-", ")"].contains(characters[position]) {
                    node.slots[1].insert(.character(characters[position]), at: node.slots[1].nodes.count)
                    position += 1
                }
                sequence.insert(node, at: sequence.nodes.count)
                continue
            }
            if character == "√" {
                position += 1
                let node = FormulaNode.squareRoot()
                if position < characters.count, characters[position] == "(" {
                    position += 1
                    parseSequence(node.slots[0], characters, &position)
                    if position < characters.count, characters[position] == ")" { position += 1 }
                } else {
                    node.slots[0].insert(.character(position < characters.count ? characters[position] : "?"), at: 0)
                    position = min(characters.count, position + 1)
                }
                sequence.insert(node, at: sequence.nodes.count)
                continue
            }
            sequence.insert(.character(character), at: sequence.nodes.count)
            position += 1
        }
    }

    private static func peekFunction(_ characters: [Character], _ position: Int) -> String? {
        for function in ["frac", "sqrt", "nroot", "pow", "asin", "acos", "atan", "sin", "cos", "tan", "ln", "log"] {
            let values = Array(function)
            guard position + values.count < characters.count else { continue }
            if Array(characters[position..<(position + values.count)]) == values, characters[position + values.count] == "(" { return function }
        }
        return nil
    }

    private static func buildFunction(_ function: String) -> FormulaNode? {
        switch function {
        case "frac": return .fraction()
        case "sqrt": return .squareRoot()
        case "nroot": return .nthRoot()
        case "pow": return .power()
        default: return nil
        }
    }

    private static func parseArguments(_ node: FormulaNode, _ characters: [Character], _ position: inout Int) {
        for slot in node.slots.indices {
            parseSequence(node.slots[slot], characters, &position)
            if slot < node.slots.count - 1, position < characters.count, characters[position] == "," { position += 1 }
        }
        if position < characters.count, characters[position] == ")" { position += 1 }
    }

    func serialize() -> String { serialize(root) }

    private func serialize(_ sequence: FormulaSequence) -> String {
        sequence.nodes.map { node in
            switch node.kind {
            case .character: return String(node.character)
            case .fraction: return "frac(\(serialize(node.slots[0])),\(serialize(node.slots[1])))"
            case .squareRoot: return "sqrt(\(serialize(node.slots[0])))"
            case .nthRoot: return "nroot(\(serialize(node.slots[0])),\(serialize(node.slots[1])))"
            case .power: return "pow(\(serialize(node.slots[0])),\(serialize(node.slots[1])))"
            }
        }.joined()
    }
}

final class FormulaCursor {
    var sequence: FormulaSequence
    var position: Int

    init(sequence: FormulaSequence, position: Int) {
        self.sequence = sequence
        self.position = position
    }

    func moveLeft() {
        if position > 0 {
            let node = sequence.nodes[position - 1]
            if node.kind != .character, let last = node.slots.last { sequence = last; position = last.nodes.count } else { position -= 1 }
            return
        }
        guard let owner = sequence.owner, let parent = owner.parentSequence, let ownerIndex = parent.nodes.firstIndex(where: { $0 === owner }) else { return }
        if sequence.slotIndex > 0 {
            sequence = owner.slots[sequence.slotIndex - 1]
            position = sequence.nodes.count
        } else {
            sequence = parent
            position = ownerIndex
        }
    }

    func moveRight() {
        if position < sequence.nodes.count {
            let node = sequence.nodes[position]
            if node.kind != .character, let first = node.slots.first { sequence = first; position = 0 } else { position += 1 }
            return
        }
        guard let owner = sequence.owner, let parent = owner.parentSequence, let ownerIndex = parent.nodes.firstIndex(where: { $0 === owner }) else { return }
        if sequence.slotIndex < owner.slots.count - 1 {
            sequence = owner.slots[sequence.slotIndex + 1]
            position = 0
        } else {
            sequence = parent
            position = ownerIndex + 1
        }
    }

    func delete() {
        guard position > 0 else { return }
        let node = sequence.nodes[position - 1]
        if node.kind != .character, let last = node.slots.last {
            sequence.nodes.remove(at: position - 1)
            var insertIndex = position - 1
            for child in last.nodes {
                sequence.insert(child, at: insertIndex)
                insertIndex += 1
            }
            position = insertIndex
        } else {
            sequence.nodes.remove(at: position - 1)
            position -= 1
        }
    }

    func insert(_ node: FormulaNode) {
        sequence.insert(node, at: position)
        if node.kind == .character { position += 1 } else if let first = node.slots.first { sequence = first; position = 0 }
    }
}

struct FormulaBox {
    let sequence: FormulaSequence
    let index: Int
    let frame: CGRect
    let baseline: CGFloat
}

final class FormulaEditorView: UIView {
    private(set) var tree = FormulaTree()
    private lazy var cursor = FormulaCursor(sequence: tree.root, position: 0)
    private var boxes: [FormulaBox] = []
    private var timer: Timer?
    private var cursorVisible = true
    var font = UIFont.systemFont(ofSize: 38)

    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = UIColor(white: 0.96, alpha: 1)
        layer.cornerRadius = 6
        layer.borderWidth = 1
        layer.borderColor = UIColor.separator.cgColor
        isUserInteractionEnabled = true
        startBlinking()
    }

    required init?(coder: NSCoder) { nil }
    deinit { timer?.invalidate() }

    var formula: String { tree.serialize() }

    func setFormula(_ value: String) {
        tree = FormulaTree.parse(value)
        cursor = FormulaCursor(sequence: tree.root, position: tree.root.nodes.count)
        setNeedsDisplay()
    }

    func handle(_ key: String) {
        switch key {
        case "__DEL__": cursor.delete()
        case "__LEFT__": cursor.moveLeft()
        case "__RIGHT__": cursor.moveRight()
        case "__FRAC__": cursor.insert(.fraction())
        case "__SQRT__": cursor.insert(.squareRoot())
        case "__NROOT__": cursor.insert(.nthRoot())
        case "__POW2__":
            let node = FormulaNode.power()
            node.slots[1].insert(.character("2"), at: 0)
            cursor.insert(node)
            cursor.moveRight()
        case "__POWA__": cursor.insert(.power())
        default: key.forEach { cursor.insert(.character($0)) }
        }
        cursorVisible = true
        setNeedsDisplay()
    }

    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        guard let point = touches.first?.location(in: self), let box = boxes.filter({ $0.frame.insetBy(dx: -5, dy: -8).contains(point) }).min(by: { abs(point.x - $0.frame.midX) < abs(point.x - $1.frame.midX) }) else { return }
        cursor.sequence = box.sequence
        cursor.position = point.x < box.frame.midX ? box.index : box.index + 1
        cursorVisible = true
        setNeedsDisplay()
    }

    override func draw(_ rect: CGRect) {
        boxes.removeAll()
        let measured = measure(tree.root, font: font)
        let x: CGFloat = 12
        let baseline = max(bounds.midY - (measured.minY + measured.maxY) * 0.5, 12 - measured.minY)
        drawSequence(tree.root, x: x, baseline: baseline, font: font)
    }

    private func startBlinking() {
        timer = Timer.scheduledTimer(withTimeInterval: 0.53, repeats: true) { [weak self] _ in self?.cursorVisible.toggle(); self?.setNeedsDisplay() }
    }

    private func attributes(_ font: UIFont) -> [NSAttributedString.Key: Any] { [.font: font, .foregroundColor: UIColor.label] }

    @discardableResult private func drawSequence(_ sequence: FormulaSequence, x: CGFloat, baseline: CGFloat, font: UIFont) -> CGFloat {
        if sequence.nodes.isEmpty {
            if cursor.sequence === sequence, cursor.position == 0, cursorVisible { drawCursor(x, baseline, font) }
            UIColor.systemGray3.setStroke()
            UIBezierPath(rect: CGRect(x: x, y: baseline - font.pointSize * 0.75, width: 12, height: font.lineHeight)).stroke()
            return 12
        }
        var currentX = x
        for index in sequence.nodes.indices {
            if cursor.sequence === sequence, cursor.position == index, cursorVisible { drawCursor(currentX, baseline, font) }
            let width = drawNode(sequence.nodes[index], sequence: sequence, index: index, x: currentX, baseline: baseline, font: font)
            currentX += width
        }
        if cursor.sequence === sequence, cursor.position == sequence.nodes.count, cursorVisible { drawCursor(currentX, baseline, font) }
        return currentX - x
    }

    private func drawNode(_ node: FormulaNode, sequence: FormulaSequence, index: Int, x: CGFloat, baseline: CGFloat, font: UIFont) -> CGFloat {
        let frame = measureNode(node, font: font).offsetBy(dx: x, dy: baseline)
        boxes.append(FormulaBox(sequence: sequence, index: index, frame: frame, baseline: baseline))
        switch node.kind {
        case .character:
            let text = String(node.character)
            text.draw(at: CGPoint(x: x, y: baseline - font.ascender), withAttributes: attributes(font))
            return (text as NSString).size(withAttributes: attributes(font)).width
        case .fraction:
            let numerator = measure(node.slots[0], font: font)
            let denominator = measure(node.slots[1], font: font)
            let width = max(numerator.width, denominator.width) + 8
            let barY = baseline - font.xHeight * 0.25
            drawSequence(node.slots[0], x: x + (width - numerator.width) * 0.5, baseline: barY - 5 - numerator.maxY, font: font)
            let path = UIBezierPath()
            path.move(to: CGPoint(x: x, y: barY))
            path.addLine(to: CGPoint(x: x + width, y: barY))
            path.lineWidth = 1.5
            UIColor.label.setStroke()
            path.stroke()
            drawSequence(node.slots[1], x: x + (width - denominator.width) * 0.5, baseline: barY + 5 - denominator.minY, font: font)
            return width
        case .squareRoot:
            return drawRoot(node.slots[0], x: x, baseline: baseline, font: font)
        case .nthRoot:
            let indexFont = font.withSize(font.pointSize * 0.55)
            let indexBounds = measure(node.slots[0], font: indexFont)
            drawSequence(node.slots[0], x: x, baseline: baseline - font.pointSize * 0.6, font: indexFont)
            return indexBounds.width + drawRoot(node.slots[1], x: x + indexBounds.width, baseline: baseline, font: font)
        case .power:
            let baseBounds = measure(node.slots[0], font: font)
            let exponentFont = font.withSize(font.pointSize * 0.65)
            drawSequence(node.slots[0], x: x, baseline: baseline, font: font)
            let exponentWidth = drawSequence(node.slots[1], x: x + baseBounds.width, baseline: baseline - font.pointSize * 0.5, font: exponentFont)
            return baseBounds.width + exponentWidth
        }
    }

    private func drawRoot(_ sequence: FormulaSequence, x: CGFloat, baseline: CGFloat, font: UIFont) -> CGFloat {
        let argument = measure(sequence, font: font)
        let width = 8 + argument.width + 4
        let top = baseline + argument.minY - 2
        let path = UIBezierPath()
        path.move(to: CGPoint(x: x, y: baseline - font.pointSize * 0.2))
        path.addLine(to: CGPoint(x: x + 3, y: baseline + font.descender * 0.5))
        path.addLine(to: CGPoint(x: x + 8, y: top))
        path.addLine(to: CGPoint(x: x + width - 2, y: top))
        path.lineWidth = 1.5
        UIColor.label.setStroke()
        path.stroke()
        drawSequence(sequence, x: x + 8, baseline: baseline, font: font)
        return width
    }

    private func drawCursor(_ x: CGFloat, _ baseline: CGFloat, _ font: UIFont) {
        let path = UIBezierPath()
        path.move(to: CGPoint(x: x, y: baseline - font.ascender))
        path.addLine(to: CGPoint(x: x, y: baseline - font.descender))
        path.lineWidth = 2
        UIColor.systemBlue.setStroke()
        path.stroke()
    }

    private func measure(_ sequence: FormulaSequence, font: UIFont) -> CGRect {
        if sequence.nodes.isEmpty { return CGRect(x: 0, y: -font.ascender, width: 12, height: font.lineHeight) }
        var width: CGFloat = 0
        var minimumY: CGFloat = 0
        var maximumY: CGFloat = 0
        for node in sequence.nodes {
            let frame = measureNode(node, font: font)
            width += frame.width
            minimumY = min(minimumY, frame.minY)
            maximumY = max(maximumY, frame.maxY)
        }
        return CGRect(x: 0, y: minimumY, width: width, height: maximumY - minimumY)
    }

    private func measureNode(_ node: FormulaNode, font: UIFont) -> CGRect {
        switch node.kind {
        case .character:
            let size = (String(node.character) as NSString).size(withAttributes: attributes(font))
            return CGRect(x: 0, y: -font.ascender, width: size.width, height: font.lineHeight)
        case .fraction:
            let numerator = measure(node.slots[0], font: font)
            let denominator = measure(node.slots[1], font: font)
            return CGRect(x: 0, y: -(numerator.height + 5.75), width: max(numerator.width, denominator.width) + 8, height: numerator.height + denominator.height + 11.5)
        case .squareRoot:
            let argument = measure(node.slots[0], font: font)
            return CGRect(x: 0, y: -(argument.height + 6), width: 12 + argument.width, height: argument.height + 6 - font.descender)
        case .nthRoot:
            let indexFont = font.withSize(font.pointSize * 0.55)
            let index = measure(node.slots[0], font: indexFont)
            let argument = measure(node.slots[1], font: font)
            return CGRect(x: 0, y: -(argument.height + 6 + index.height * 0.3), width: index.width + 12 + argument.width, height: argument.height + 6 + index.height * 0.3 - font.descender)
        case .power:
            let base = measure(node.slots[0], font: font)
            let exponentFont = font.withSize(font.pointSize * 0.65)
            let exponent = measure(node.slots[1], font: exponentFont)
            return CGRect(x: 0, y: min(base.minY, -(font.pointSize * 0.5 + exponent.height)), width: base.width + exponent.width, height: max(base.maxY, -font.descender) - min(base.minY, -(font.pointSize * 0.5 + exponent.height)))
        }
    }
}

final class FormulaKeyboardView: UIView {
    weak var editor: FormulaEditorView?
    private let rows = [
        [("sin", "sin("), ("asin", "asin("), ("7", "7"), ("8", "8"), ("9", "9"), ("×", "*"), ("a/b", "__FRAC__")],
        [("cos", "cos("), ("acos", "acos("), ("4", "4"), ("5", "5"), ("6", "6"), ("+", "+"), ("−", "-")],
        [("tan", "tan("), ("atan", "atan("), ("1", "1"), ("2", "2"), ("3", "3"), ("⌫", "__DEL__"), ("π", "π")],
        [("ln", "ln("), ("lg", "log("), ("0", "0"), (".", "."), ("←", "__LEFT__"), ("→", "__RIGHT__"), ("ℯ", "e")],
        [("√", "__SQRT__"), ("ⁿ√", "__NROOT__"), ("x²", "__POW2__"), ("xᵃ", "__POWA__"), ("(", "("), (")", ")"), ("%", "%")]
    ]

    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = UIColor(white: 0.93, alpha: 1)
        let vertical = UIStackView()
        vertical.axis = .vertical
        vertical.spacing = 4
        vertical.distribution = .fillEqually
        vertical.translatesAutoresizingMaskIntoConstraints = false
        addSubview(vertical)
        NSLayoutConstraint.activate([vertical.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 5), vertical.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -5), vertical.topAnchor.constraint(equalTo: topAnchor, constant: 5), vertical.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -5)])
        for row in rows {
            let horizontal = UIStackView()
            horizontal.axis = .horizontal
            horizontal.spacing = 4
            horizontal.distribution = .fillEqually
            for key in row {
                let button = UIButton(type: .system)
                button.setTitle(key.0, for: .normal)
                button.accessibilityIdentifier = key.1
                button.backgroundColor = key.1 == "__DEL__" ? UIColor.systemRed.withAlphaComponent(0.25) : (key.1 == "__FRAC__" ? UIColor.systemPurple.withAlphaComponent(0.25) : .systemBackground)
                button.layer.cornerRadius = 5
                button.addTarget(self, action: #selector(pressed(_:)), for: .touchUpInside)
                horizontal.addArrangedSubview(button)
            }
            vertical.addArrangedSubview(horizontal)
        }
    }

    required init?(coder: NSCoder) { nil }
    @objc private func pressed(_ sender: UIButton) { editor?.handle(sender.accessibilityIdentifier ?? "") }
}

final class FormulaInputViewController: UIViewController {
    let editor = FormulaEditorView()
    let keyboard = FormulaKeyboardView()
    var onConfirm: ((String) -> Void)?
    private let initial: String

    init(title: String, initial: String) {
        self.initial = initial
        super.init(nibName: nil, bundle: nil)
        self.title = title
        preferredContentSize = CGSize(width: 720, height: 470)
    }

    required init?(coder: NSCoder) { nil }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        keyboard.editor = editor
        editor.setFormula(initial)
        editor.translatesAutoresizingMaskIntoConstraints = false
        keyboard.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(editor)
        view.addSubview(keyboard)
        NSLayoutConstraint.activate([
            editor.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            editor.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            editor.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 12),
            editor.heightAnchor.constraint(equalToConstant: 120),
            keyboard.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            keyboard.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            keyboard.topAnchor.constraint(equalTo: editor.bottomAnchor, constant: 12),
            keyboard.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor)
        ])
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: "取消", style: .plain, target: self, action: #selector(cancel))
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "确定", style: .done, target: self, action: #selector(confirm))
    }

    @objc private func cancel() { dismiss(animated: true) }
    @objc private func confirm() {
        let value = editor.formula
        let callback = onConfirm
        dismiss(animated: true) { callback?(value) }
    }
}
