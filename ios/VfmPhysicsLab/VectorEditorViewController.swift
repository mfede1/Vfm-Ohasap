import UIKit

final class VectorEditorViewController: UITableViewController {
    let object: PhysicsObject
    let engine: PhysicsEngine
    let force: Bool
    var magnitude: CGFloat
    var angle: CGFloat
    var condition: LogicCondition?
    var onApply: (() -> Void)?
    var onSelectConditionTarget: ((PhysicsObject, @escaping (PhysicsObject?) -> Void) -> Void)?

    init(object: PhysicsObject, engine: PhysicsEngine, force: Bool, magnitude: CGFloat, angle: CGFloat) {
        self.object = object
        self.engine = engine
        self.force = force
        self.magnitude = magnitude
        self.angle = angle
        self.condition = force ? object.forceCondition : object.velocityCondition
        super.init(style: .insetGrouped)
        title = force ? "施加力" : "设置初速度"
    }

    required init?(coder: NSCoder) { nil }

    override func viewDidLoad() {
        super.viewDidLoad()
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: "取消", style: .cancel, target: self, action: #selector(cancel))
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "确认", style: .done, target: self, action: #selector(confirm))
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { 3 }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = UITableViewCell(style: .value1, reuseIdentifier: nil)
        switch indexPath.row {
        case 0:
            cell.textLabel?.text = force ? "大小 (N)" : "大小 (m/s)"
            cell.detailTextLabel?.text = SurdFormatter.text(magnitude, places: engine.decimalPlaces)
        case 1:
            cell.textLabel?.text = "方向 (°)"
            cell.detailTextLabel?.text = SurdFormatter.text(angle, places: engine.decimalPlaces)
        default:
            cell.textLabel?.text = force ? "受该力直到" : "保持速度直到"
            cell.detailTextLabel?.text = condition?.describe() ?? "永久"
        }
        cell.accessoryType = .disclosureIndicator
        return cell
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if indexPath.row == 0 {
            promptExpression(title: force ? "大小 (N)" : "大小 (m/s)", current: SurdFormatter.text(magnitude, places: engine.decimalPlaces)) { [weak self] value in self?.magnitude = max(0, value); self?.tableView.reloadData() }
        } else if indexPath.row == 1 {
            promptExpression(title: "方向 (°)", current: SurdFormatter.text(angle, places: engine.decimalPlaces)) { [weak self] value in self?.angle = value.truncatingRemainder(dividingBy: 360); self?.tableView.reloadData() }
        } else {
            let alert = UIAlertController(title: "直到", message: nil, preferredStyle: .actionSheet)
            alert.addAction(UIAlertAction(title: "永久", style: .default) { [weak self] _ in self?.condition = nil; self?.tableView.reloadData() })
            alert.addAction(UIAlertAction(title: "选择逻辑块", style: .default) { [weak self] _ in self?.showConditionBuilder() })
            alert.addAction(UIAlertAction(title: "取消", style: .cancel))
            alert.popoverPresentationController?.sourceView = view
            alert.popoverPresentationController?.sourceRect = CGRect(x: view.bounds.midX, y: view.bounds.midY, width: 1, height: 1)
            present(alert, animated: true)
        }
    }

    private func showConditionBuilder() {
        let builder = ConditionBuilderViewController(owner: object, engine: engine)
        if onSelectConditionTarget != nil {
            builder.onRequestTarget = { [weak self, weak builder] completion in
                guard let self, let builder, let builderNavigation = builder.navigationController, let vectorNavigation = self.navigationController, let presenter = vectorNavigation.presentingViewController else { return }
                builderNavigation.dismiss(animated: true) { [weak self] in
                    guard let self else { return }
                    vectorNavigation.dismiss(animated: true) { [weak self] in
                        guard let self else { return }
                        self.onSelectConditionTarget?(self.object) { [weak self] target in
                            guard let self else { return }
                            presenter.present(vectorNavigation, animated: true) {
                                self.present(builderNavigation, animated: true) { completion(target) }
                            }
                        }
                    }
                }
            }
        }
        builder.onComplete = { [weak self, weak builder] condition in
            self?.condition = condition
            builder?.dismiss(animated: true)
            self?.tableView.reloadData()
        }
        let navigation = UINavigationController(rootViewController: builder)
        navigation.modalPresentationStyle = .formSheet
        present(navigation, animated: true)
    }

    @objc private func cancel() { dismiss(animated: true) }

    @objc private func confirm() {
        let radians = angle * .pi / 180
        let vector = CGVector(dx: cos(radians) * magnitude, dy: -sin(radians) * magnitude)
        if force {
            if engine.isFreeEndConstraint(object) {
                object.freeAppliedForce = vector
            } else if !engine.isConstraintBody(object) {
                object.appliedForce = vector
                object.forceCondition = condition
                object.forceTimer = 0
            }
        } else {
            if engine.isFreeEndConstraint(object) {
                object.freeEndVelocity = vector
            } else if !engine.isConstraintBody(object) {
                object.velocity = vector
                object.initialVelocity = vector
                object.velocityCondition = condition
            }
        }
        onApply?()
        dismiss(animated: true)
    }
}
