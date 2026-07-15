import UIKit

final class MathKeyboardInputView: UIInputView {
    private weak var textField: UITextField?
    private let rows = [
        ["7", "8", "9", "÷", "√"],
        ["4", "5", "6", "×", "^"],
        ["1", "2", "3", "−", "!"],
        ["0", ".", "π", "+", "⌫"],
        ["(", ")", "e", "清除", "完成"]
    ]

    init(textField: UITextField) {
        self.textField = textField
        super.init(frame: CGRect(x: 0, y: 0, width: 640, height: 260), inputViewStyle: .keyboard)
        autoresizingMask = .flexibleWidth
        configure()
    }

    required init?(coder: NSCoder) { nil }

    private func configure() {
        let vertical = UIStackView()
        vertical.axis = .vertical
        vertical.spacing = 5
        vertical.distribution = .fillEqually
        vertical.translatesAutoresizingMaskIntoConstraints = false
        addSubview(vertical)
        NSLayoutConstraint.activate([
            vertical.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 6),
            vertical.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -6),
            vertical.topAnchor.constraint(equalTo: topAnchor, constant: 6),
            vertical.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -6)
        ])
        for row in rows {
            let horizontal = UIStackView()
            horizontal.axis = .horizontal
            horizontal.spacing = 5
            horizontal.distribution = .fillEqually
            for title in row {
                let button = UIButton(type: .system)
                button.setTitle(title, for: .normal)
                button.titleLabel?.font = .systemFont(ofSize: 18, weight: .medium)
                button.backgroundColor = title == "完成" ? .systemBlue : UIColor.secondarySystemBackground
                button.setTitleColor(title == "完成" ? .white : .label, for: .normal)
                button.layer.cornerRadius = 6
                button.addTarget(self, action: #selector(keyPressed(_:)), for: .touchUpInside)
                horizontal.addArrangedSubview(button)
            }
            vertical.addArrangedSubview(horizontal)
        }
    }

    @objc private func keyPressed(_ sender: UIButton) {
        guard let title = sender.currentTitle, let textField else { return }
        switch title {
        case "⌫": textField.deleteBackward()
        case "清除": textField.text = ""
        case "完成": textField.resignFirstResponder()
        case "×": textField.insertText("*")
        case "÷": textField.insertText("/")
        case "−": textField.insertText("-")
        case "√": textField.insertText("sqrt(")
        default: textField.insertText(title)
        }
    }
}
