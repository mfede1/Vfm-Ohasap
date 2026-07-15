import UIKit

final class ConditionBuilderViewController: UITableViewController {
    let owner: PhysicsObject
    let engine: PhysicsEngine
    var onComplete: ((LogicCondition) -> Void)?
    var onRequestTarget: ((@escaping (PhysicsObject?) -> Void) -> Void)?
    private let choices: [(String, ConditionKind)] = [
        ("1. 经过...时间", .afterTime),
        ("2. 接触...物体", .contactObject),
        ("3. 至...时间", .timerReach),
        ("4. ...物体不存在", .objectMissing),
        ("5. 接触...物体...时间（连续）", .contactDuration),
        ("6. 接触...物体总计...时间", .contactTotal),
        ("7. 第...次接触...物体...时间后", .afterNthContact),
        ("复合逻辑", .compound)
    ]

    init(owner: PhysicsObject, engine: PhysicsEngine) {
        self.owner = owner
        self.engine = engine
        super.init(style: .insetGrouped)
        title = "选择触发逻辑"
    }

    required init?(coder: NSCoder) { nil }

    override func viewDidLoad() {
        super.viewDidLoad()
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: "取消", style: .plain, target: self, action: #selector(cancel))
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { choices.count }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = UITableViewCell(style: .default, reuseIdentifier: nil)
        cell.textLabel?.text = choices[indexPath.row].0
        cell.accessoryType = .disclosureIndicator
        return cell
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let kind = choices[indexPath.row].1
        if kind == .compound {
            let compound = CompoundConditionViewController(owner: owner, engine: engine)
            compound.onComplete = onComplete
            compound.onRequestTarget = onRequestTarget
            navigationController?.pushViewController(compound, animated: true)
        } else { configure(kind) }
    }

    private func configure(_ kind: ConditionKind) {
        let condition = LogicCondition()
        condition.kind = kind
        let needsTarget = [ConditionKind.contactObject, .objectMissing, .contactDuration, .contactTotal, .afterNthContact].contains(kind)
        if needsTarget {
            let targets = engine.objects.filter { $0 !== owner }
            guard !targets.isEmpty else {
                let alert = UIAlertController(title: nil, message: "场景中没有可选择的物体", preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "确定", style: .default))
                present(alert, animated: true)
                return
            }
            if let onRequestTarget {
                onRequestTarget { [weak self] target in
                    guard let self, let target, target !== self.owner else { return }
                    condition.targetID = target.id
                    self.configureParameters(condition)
                }
                return
            }
            let picker = UIAlertController(title: "选择物体", message: nil, preferredStyle: .actionSheet)
            for (index, target) in targets.enumerated() {
                let title = "\(index + 1). \(target.kind.name) (\(Int(target.position.x)), \(Int(target.position.y)))"
                picker.addAction(UIAlertAction(title: title, style: .default) { [weak self, weak target] _ in
                    guard let self, let target else { return }
                    condition.targetID = target.id
                    self.configureParameters(condition)
                })
            }
            picker.addAction(UIAlertAction(title: "取消", style: .cancel))
            picker.popoverPresentationController?.sourceView = view
            picker.popoverPresentationController?.sourceRect = CGRect(x: view.bounds.midX, y: view.bounds.midY, width: 1, height: 1)
            present(picker, animated: true)
        } else { configureParameters(condition) }
    }

    private func configureParameters(_ condition: LogicCondition) {
        if condition.kind == .afterNthContact {
            promptExpression(title: "接触次数（-1为最后一次）", current: "1") { [weak self] value in
                condition.contactNth = Int(value.rounded())
                self?.askTime(condition)
            }
        } else if [.afterTime, .timerReach, .contactDuration, .contactTotal].contains(condition.kind) {
            askTime(condition)
        } else { onComplete?(condition) }
    }

    private func askTime(_ condition: LogicCondition) {
        promptExpression(title: "时间 (秒)", current: "1") { [weak self] value in
            condition.time = max(0, value)
            self?.onComplete?(condition)
        }
    }

    @objc private func cancel() { dismiss(animated: true) }
}

final class CompoundConditionViewController: UITableViewController {
    let owner: PhysicsObject
    let engine: PhysicsEngine
    var conditions: [LogicCondition] = []
    var requiredCount = 1
    var onComplete: ((LogicCondition) -> Void)?
    var onRequestTarget: ((@escaping (PhysicsObject?) -> Void) -> Void)?

    init(owner: PhysicsObject, engine: PhysicsEngine) {
        self.owner = owner
        self.engine = engine
        super.init(style: .insetGrouped)
        title = "复合逻辑"
    }

    required init?(coder: NSCoder) { nil }

    override func viewDidLoad() {
        super.viewDidLoad()
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "确定", style: .done, target: self, action: #selector(confirm))
    }

    override func numberOfSections(in tableView: UITableView) -> Int { 2 }
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { section == 0 ? conditions.count + (conditions.count < 5 ? 1 : 0) : 1 }
    override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? { section == 0 ? "逻辑（1-5个）" : "所需同时生效逻辑数" }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = UITableViewCell(style: .value1, reuseIdentifier: nil)
        if indexPath.section == 0 {
            if indexPath.row == conditions.count { cell.textLabel?.text = "添加逻辑"; cell.textLabel?.textColor = .systemBlue }
            else { cell.textLabel?.text = "第\(indexPath.row + 1)个"; cell.detailTextLabel?.text = conditions[indexPath.row].describe() }
        } else {
            cell.textLabel?.text = "至少生效"
            let stepper = UIStepper()
            stepper.minimumValue = 1
            stepper.maximumValue = Double(max(1, conditions.count))
            stepper.value = Double(min(requiredCount, max(1, conditions.count)))
            stepper.addTarget(self, action: #selector(stepperChanged(_:)), for: .valueChanged)
            cell.detailTextLabel?.text = String(Int(stepper.value))
            cell.accessoryView = stepper
        }
        return cell
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        guard indexPath.section == 0, indexPath.row == conditions.count, conditions.count < 5 else { return }
        let builder = ConditionBuilderViewController(owner: owner, engine: engine)
        builder.navigationItem.leftBarButtonItem = nil
        builder.onRequestTarget = onRequestTarget
        builder.onComplete = { [weak self, weak builder] condition in
            self?.conditions.append(condition)
            self?.requiredCount = max(1, min(self?.requiredCount ?? 1, self?.conditions.count ?? 1))
            builder?.navigationController?.popViewController(animated: true)
            self?.tableView.reloadData()
        }
        navigationController?.pushViewController(builder, animated: true)
    }

    @objc private func stepperChanged(_ sender: UIStepper) { requiredCount = Int(sender.value); tableView.reloadSections(IndexSet(integer: 1), with: .none) }

    @objc private func confirm() {
        guard !conditions.isEmpty else { return }
        let compound = LogicCondition()
        compound.kind = .compound
        compound.subconditions = conditions
        compound.requiredCount = max(1, min(requiredCount, conditions.count))
        onComplete?(compound)
    }
}
