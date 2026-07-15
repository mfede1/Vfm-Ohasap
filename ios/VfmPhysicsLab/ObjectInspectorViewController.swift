import UIKit

final class ObjectInspectorViewController: UITableViewController {
    let object: PhysicsObject
    let engine: PhysicsEngine
    var onChange: (() -> Void)?
    var onDelete: (() -> Void)?
    var onWrapDrawing: ((PhysicsObject) -> Void)?
    var onClose: (() -> Void)?
    var onConnect: ((PhysicsObject, Bool) -> Void)?
    var onSelectPulleyRamp: ((PhysicsObject) -> Void)?
    var onRotate: ((PhysicsObject) -> Void)?
    var onSelectConditionTarget: ((PhysicsObject, @escaping (PhysicsObject?) -> Void) -> Void)?
    private var numericRows: [NumericProperty] = []
    private let original: EditableObjectSnapshot

    init(object: PhysicsObject, engine: PhysicsEngine) {
        self.object = object
        self.engine = engine
        self.original = EditableObjectSnapshot(object)
        super.init(style: .insetGrouped)
        if object.kind == .pulley {
            title = object.fixed || object.pulleyRamp != nil ? "定滑轮" : (engine.objects.contains { $0.kind == .rope && $0.ropeWrapped } ? "动滑轮" : "滑轮")
        } else {
            title = object.kind.name
        }
        numericRows = makeNumericRows()
    }

    required init?(coder: NSCoder) { nil }

    override func viewDidLoad() {
        super.viewDidLoad()
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "确定", style: .done, target: self, action: #selector(done))
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: "取消", style: .plain, target: self, action: #selector(cancel))
        navigationController?.isModalInPresentation = true
        tableView.register(UITableViewCell.self, forCellReuseIdentifier: "cell")
    }

    override func numberOfSections(in tableView: UITableView) -> Int { 4 }

    override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        ["物理参数", "尺寸与形态", "显示与约束", "操作"][section]
    }

    override func tableView(_ tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        guard section == 1 else { return nil }
        if object.kind == .spring || object.kind == .rope { return "最大形变量: \(SurdFormatter.text(object.elasticLimit / max(1, object.springConstant), places: engine.decimalPlaces)) m" }
        if object.kind == .container {
            let waterMass = 1000 * max(0, object.realWidth - 2 * object.wallThickness) * object.waterLevel * 0.3
            return "密度: \(SurdFormatter.text(object.density, places: engine.decimalPlaces)) kg/m³\n水质量≈\(SurdFormatter.text(waterMass, places: engine.decimalPlaces)) kg"
        }
        if object.kind == .straightRamp {
            let angle = atan2(object.realHeight, object.realWidth) * 180 / .pi
            return "角度: \(SurdFormatter.text(angle, places: engine.decimalPlaces))°\n密度: \(SurdFormatter.text(object.density, places: engine.decimalPlaces)) kg/m³"
        }
        if object.kind == .ball || object.kind == .block || object.kind == .board || object.kind == .arcRamp { return "密度: \(SurdFormatter.text(object.density, places: engine.decimalPlaces)) kg/m³" }
        return nil
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch section {
        case 0: return physicalRowCount
        case 1: return max(0, numericRows.count - physicalRowCount)
        case 2: return toggleRows.count
        default: return actionRows.count
        }
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = UITableViewCell(style: .value1, reuseIdentifier: nil)
        if indexPath.section < 2 {
            let index = indexPath.section == 0 ? indexPath.row : indexPath.row + physicalRowCount
            let property = numericRows[index]
            cell.textLabel?.text = property.title
            let accessory = UIView(frame: CGRect(x: 0, y: 0, width: 150, height: 34))
            let slider = UISlider(frame: CGRect(x: 0, y: 2, width: 98, height: 30))
            slider.minimumValue = Float(property.minimum)
            slider.maximumValue = Float(property.maximum)
            slider.value = Float(property.get())
            slider.tag = index
            slider.addTarget(self, action: #selector(numericSliderChanged(_:)), for: .valueChanged)
            slider.addTarget(self, action: #selector(numericSliderFinished(_:)), for: [.touchUpInside, .touchUpOutside, .touchCancel])
            let valueLabel = UILabel(frame: CGRect(x: 102, y: 0, width: 48, height: 34))
            valueLabel.tag = 999
            valueLabel.font = .monospacedDigitSystemFont(ofSize: 9, weight: .regular)
            valueLabel.textAlignment = .right
            valueLabel.adjustsFontSizeToFitWidth = true
            valueLabel.text = String(format: "%.4g", Double(property.get()))
            accessory.addSubview(slider)
            accessory.addSubview(valueLabel)
            cell.accessoryView = accessory
            cell.selectionStyle = .default
        } else if indexPath.section == 2 {
            let row = toggleRows[indexPath.row]
            cell.textLabel?.text = row.title
            let toggle = UISwitch()
            toggle.isOn = row.get()
            toggle.tag = indexPath.row
            toggle.addTarget(self, action: #selector(toggleChanged(_:)), for: .valueChanged)
            cell.accessoryView = toggle
        } else {
            cell.textLabel?.text = actionRows[indexPath.row]
            cell.textLabel?.textColor = actionRows[indexPath.row] == "删除" ? .systemRed : .label
            cell.accessoryType = .disclosureIndicator
        }
        return cell
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if indexPath.section < 2 {
            let index = indexPath.section == 0 ? indexPath.row : indexPath.row + physicalRowCount
            let property = numericRows[index]
            promptExpression(title: property.title, current: String(format: "%.8g", Double(property.get()))) { [weak self] value in
                guard let self else { return }
                property.set(max(property.minimum, min(property.maximum, value)))
                if self.propertyAffectsSize(property) { self.engine.updateObjectSize(self.object) }
                self.tableView.reloadData()
                self.onChange?()
            }
            return
        }
        guard indexPath.section == 3 else { return }
        performAction(actionRows[indexPath.row])
    }

    @objc private func toggleChanged(_ sender: UISwitch) {
        toggleRows[sender.tag].set(sender.isOn)
        if toggleRows[sender.tag].title == "固定角度", sender.isOn { object.fixedAngle = object.rotation }
        onChange?()
    }

    @objc private func numericSliderChanged(_ sender: UISlider) {
        guard numericRows.indices.contains(sender.tag) else { return }
        let property = numericRows[sender.tag]
        let value = max(property.minimum, min(property.maximum, CGFloat(sender.value)))
        property.set(value)
        if propertyAffectsSize(property) { engine.updateObjectSize(object) }
        (sender.superview?.viewWithTag(999) as? UILabel)?.text = String(format: "%.4g", Double(value))
        onChange?()
    }

    @objc private func numericSliderFinished(_ sender: UISlider) {
        tableView.reloadData()
    }

    private func propertyAffectsSize(_ property: NumericProperty) -> Bool {
        property.title == "长度(m)" || property.title == "高度(m)" || property.title == "半径(m)" || property.title == "直径(m)"
    }

    private var toggleRows: [ToggleProperty] {
        var rows = [
            ToggleProperty(title: object.kind == .rod ? "远程锁定" : ((object.kind == .spring || object.kind == .rope) ? "限制" : "固定"), get: { [object] in object.kind == .rod ? object.remoteLock : object.fixed }, set: { [object] in if object.kind == .rod { object.remoteLock = $0 } else { object.fixed = $0 } }),
            ToggleProperty(title: "显示状态", get: { [object] in object.showState }, set: { [object] in object.showState = $0 }),
            ToggleProperty(title: "受力分析", get: { [object] in object.showForces }, set: { [object] in object.showForces = $0 }),
            ToggleProperty(title: "轨迹", get: { [object] in object.showTrajectory }, set: { [object] in object.showTrajectory = $0 })
        ]
        if object.kind == .rod || object.kind == .spring || object.kind == .rope {
            rows.append(ToggleProperty(title: "竖直方向", get: { [object] in !object.horizontal }, set: { [object] in object.horizontal = !$0; object.rotation = $0 ? .pi * 0.5 : 0; if object.angleFixed { object.fixedAngle = object.rotation } }))
            rows.append(ToggleProperty(title: "固定角度", get: { [object] in object.angleFixed }, set: { [object] in object.angleFixed = $0 }))
        }
        if object.kind == .arcRamp {
            rows.append(ToggleProperty(title: "管道", get: { [object] in object.pipeMode }, set: { [object] in object.pipeMode = $0 }))
        }
        if object.kind == .container {
            rows.append(ToggleProperty(title: "水体", get: { [object] in object.waterEnabled }, set: { [object] in object.waterEnabled = $0 }))
        }
        if object.kind == .straightRamp || object.kind == .arcRamp {
            rows.append(ToggleProperty(title: "斜面向右", get: { [object] in object.horizontal }, set: { [object] in object.horizontal = $0 }))
        }
        return rows
    }

    private var physicalRowCount: Int { object.kind == .spring || object.kind == .rope || object.kind == .rod ? 1 : min(3, numericRows.count) }

    private var actionRows: [String] {
        var rows = ["设置初速度", "施加力"]
        if object.kind == .spring || object.kind == .rope || object.kind == .rod {
            rows.append(object.anchorA == .none ? "绑定端点A" : "取消绑定端点A")
            rows.append(object.anchorB == .none ? "绑定端点B" : "取消绑定端点B")
        }
        if object.kind == .rope { rows.append(object.ropeWrapped ? "取消缠绕" : "缠绕") }
        if object.kind == .spring || object.kind == .rope || object.kind == .rod { rows.append("旋转") }
        if object.kind == .rod { rows.append(object.rodRing == nil ? "添加环" : "移除环") }
        if object.kind == .pulley { rows.append(object.pulleyRamp == nil ? "绑定直斜面顶角" : "取消绑定直斜面") }
        rows += ["触发逻辑", "查看逻辑", "删除"]
        return rows
    }

    private func makeNumericRows() -> [NumericProperty] {
        var rows = [NumericProperty(title: "质量(kg)", minimum: 0.1, maximum: 20, get: { [object] in object.mass }, set: { [object] in
            object.mass = $0
            if object.kind == .ball {
                let volume = 4 / 3 * CGFloat.pi * pow(object.realRadius, 3)
                object.density = volume > 0 ? object.mass / volume : 500
            } else if object.kind != .spring && object.kind != .rope && object.kind != .pulley && object.kind != .rod && object.kind != .ring {
                object.density = object.baseArea > 0 ? object.mass / (object.baseArea * max(0.1, object.realHeight)) : 500
            }
        })]
        if object.kind != .spring && object.kind != .rope && object.kind != .rod {
            rows.append(NumericProperty(title: object.kind == .ring ? "内表面摩擦系数" : (object.kind == .ball || object.kind == .pulley ? "摩擦系数" : "下表面摩擦系数"), minimum: 0, maximum: 1, get: { [object] in object.friction }, set: { [object] in object.friction = $0 }))
            rows.append(NumericProperty(title: "恢复系数", minimum: 0, maximum: 1, get: { [object] in object.restitution }, set: { [object] in object.restitution = $0 }))
        }
        switch object.kind {
        case .block, .board, .straightRamp:
            rows.append(NumericProperty(title: "长度(m)", minimum: 0.1, maximum: 5, get: { [object] in object.realWidth }, set: { [object] in object.realWidth = $0 }))
            rows.append(NumericProperty(title: "高度(m)", minimum: 0.1, maximum: 5, get: { [object] in object.realHeight }, set: { [object] in object.realHeight = $0 }))
        case .container:
            rows.append(NumericProperty(title: "长度(m)", minimum: 0.2, maximum: 5, get: { [object] in object.realWidth }, set: { [object] in object.realWidth = $0 }))
            rows.append(NumericProperty(title: "高度(m)", minimum: 0.1, maximum: 3, get: { [object] in object.realHeight }, set: { [object] in object.realHeight = $0 }))
        case .arcRamp:
            rows.append(NumericProperty(title: "长度(m)", minimum: 0, maximum: 5, get: { [object] in object.realWidth }, set: { [object] in object.realWidth = $0 }))
            rows.append(NumericProperty(title: "高度(m)", minimum: 0.1, maximum: 5, get: { [object] in object.realHeight }, set: { [object] in object.realHeight = $0 }))
            rows.append(NumericProperty(title: "弧度(rad)", minimum: .pi, maximum: 2 * .pi, get: { [object] in object.arcAngle }, set: { [object] in object.arcAngle = $0 }))
            rows.append(NumericProperty(title: "管道直径(m)", minimum: 0.05, maximum: 5, get: { [object] in object.pipeDiameter }, set: { [object] in object.pipeDiameter = $0 }))
        case .ball, .pulley, .ring:
            rows.append(NumericProperty(title: "半径(m)", minimum: 0.05, maximum: 1, get: { [object] in object.realRadius }, set: { [object] in
                object.realRadius = $0
                if object.kind == .ball {
                    let volume = 4 / 3 * CGFloat.pi * pow(object.realRadius, 3)
                    object.density = volume > 0 ? object.mass / volume : 500
                }
            }))
        case .spring:
            rows.append(NumericProperty(title: "长度(m)", minimum: 0.2, maximum: 5, get: { [object] in object.realLength }, set: { [object] in object.realLength = $0 }))
            rows.append(NumericProperty(title: "劲度系数(N/m)", minimum: 100, maximum: 2000, get: { [object] in object.springConstant }, set: { [object] in object.springConstant = $0 }))
            rows.append(NumericProperty(title: "弹性限度(N)", minimum: 150, maximum: 1200, get: { [object] in object.elasticLimit }, set: { [object] in object.elasticLimit = $0 }))
            rows.append(NumericProperty(title: "角度(°)", minimum: 0, maximum: 360, get: { [object] in object.rotation * 180 / .pi }, set: { [object] in object.rotation = $0 * .pi / 180 }))
        case .rope:
            rows.append(NumericProperty(title: "长度(m)", minimum: 0.2, maximum: 5, get: { [object] in object.realLength }, set: { [object] in object.realLength = $0 }))
            rows.append(NumericProperty(title: "劲度系数(N/m)", minimum: 100, maximum: 2000, get: { [object] in object.springConstant }, set: { [object] in object.springConstant = $0 }))
            rows.append(NumericProperty(title: "弹性限度(N)", minimum: 300, maximum: 2400, get: { [object] in object.elasticLimit }, set: { [object] in object.elasticLimit = $0 }))
            rows.append(NumericProperty(title: "角度(°)", minimum: 0, maximum: 360, get: { [object] in object.rotation * 180 / .pi }, set: { [object] in object.rotation = $0 * .pi / 180 }))
        case .rod:
            rows.append(NumericProperty(title: "长度(m)", minimum: 0.2, maximum: 5, get: { [object] in object.realLength }, set: { [object] in object.realLength = $0 }))
            rows.append(NumericProperty(title: "直径(m)", minimum: 0, maximum: 1, get: { [object] in object.realHeight }, set: { [object] in object.realHeight = $0 }))
            rows.append(NumericProperty(title: "角度(°)", minimum: 0, maximum: 360, get: { [object] in object.rotation * 180 / .pi }, set: { [object] in object.rotation = $0 * .pi / 180 }))
        }
        if object.kind == .container {
            rows.append(NumericProperty(title: "容器壁厚(m)", minimum: 0, maximum: 0.1, get: { [object] in object.wallThickness }, set: { [object] in object.wallThickness = $0 }))
            rows.append(NumericProperty(title: "水位(m)", minimum: 0, maximum: object.realHeight, get: { [object] in object.waterLevel }, set: { [object] in object.waterLevel = $0 }))
            rows.append(NumericProperty(title: "底面积(m²)", minimum: 0.01, maximum: 2, get: { [object] in object.baseArea }, set: { [object] in object.baseArea = $0 }))
        } else if object.kind == .block || object.kind == .board || object.kind == .straightRamp || object.kind == .arcRamp {
            rows.append(NumericProperty(title: "底面积(m²)", minimum: 0.01, maximum: 2, get: { [object] in object.baseArea }, set: { [object] in object.baseArea = $0; object.density = object.mass / ($0 * max(0.1, object.realHeight)) }))
        }
        return rows
    }

    private func performAction(_ title: String) {
        switch title {
        case "设置初速度": showVectorEditor(force: false)
        case "施加力": showVectorEditor(force: true)
        case "绑定端点A": onConnect?(object, true)
        case "绑定端点B": onConnect?(object, false)
        case "取消绑定端点A": engine.setAnchor(.none, on: object, endA: true); object.bindACondition = nil; engine.refreshConstraintPose(object); onChange?(); tableView.reloadData()
        case "取消绑定端点B": engine.setAnchor(.none, on: object, endA: false); object.bindBCondition = nil; engine.refreshConstraintPose(object); onChange?(); tableView.reloadData()
        case "缠绕": onWrapDrawing?(object)
        case "取消缠绕": object.ropeWrapped = false; object.wrapPoints.removeAll(); object.ropeParticles.removeAll(); object.ropeParticlesInitialized = false; onChange?(); tableView.reloadData()
        case "旋转": onRotate?(object)
        case "添加环": addRing()
        case "移除环": if let ring = object.rodRing { engine.remove(ring) }; object.rodRing = nil; onChange?(); tableView.reloadData()
        case "绑定直斜面顶角": onSelectPulleyRamp?(object)
        case "取消绑定直斜面": object.pulleyRamp = nil; object.bindACondition = nil; onChange?(); tableView.reloadData()
        case "触发逻辑": chooseLogicTarget()
        case "查看逻辑": showLogicSummary()
        case "删除": engine.remove(object); onDelete?(); close()
        default: break
        }
    }

    private func showVectorEditor(force: Bool) {
        let vector = force ? object.appliedForce : object.velocity
        let magnitude = vector.length
        var angle = atan2(-vector.dy, vector.dx) * 180 / .pi
        if angle < 0 { angle += 360 }
        let editor = VectorEditorViewController(object: object, engine: engine, force: force, magnitude: magnitude, angle: angle)
        editor.onApply = onChange
        editor.onSelectConditionTarget = onSelectConditionTarget
        let navigation = UINavigationController(rootViewController: editor)
        navigation.modalPresentationStyle = .formSheet
        present(navigation, animated: true)
    }

    private func chooseAnchor(endA: Bool) {
        let alert = UIAlertController(title: endA ? "绑定端点A" : "绑定端点B", message: nil, preferredStyle: .actionSheet)
        for candidate in engine.objects where candidate !== object && candidate.kind != .spring && candidate.kind != .rope && candidate.kind != .rod {
            alert.addAction(UIAlertAction(title: candidate.kind.name, style: .default) { [weak self, weak candidate] _ in
                guard let self, let candidate else { return }
                self.assign(.object(candidate.id), endA: endA)
            })
        }
        alert.addAction(UIAlertAction(title: "天花板", style: .default) { [weak self] _ in
            guard let self, let index = self.engine.ceilings.indices.first else { return }
            self.assign(.ceiling(index, self.object.position.x), endA: endA)
        })
        alert.addAction(UIAlertAction(title: "地面", style: .default) { [weak self] _ in guard let self else { return }; self.assign(.ground(self.object.position.x), endA: endA) })
        alert.addAction(UIAlertAction(title: "左墙", style: .default) { [weak self] _ in guard let self else { return }; self.assign(.leftWall(self.object.position.y), endA: endA) })
        alert.addAction(UIAlertAction(title: "右墙", style: .default) { [weak self] _ in guard let self else { return }; self.assign(.rightWall(self.object.position.y), endA: endA) })
        alert.addAction(UIAlertAction(title: "取消绑定", style: .destructive) { [weak self] _ in self?.assign(.none, endA: endA) })
        alert.addAction(UIAlertAction(title: "取消", style: .cancel))
        alert.popoverPresentationController?.sourceView = view
        alert.popoverPresentationController?.sourceRect = CGRect(x: view.bounds.midX, y: view.bounds.midY, width: 1, height: 1)
        present(alert, animated: true)
    }

    private func assign(_ anchor: Anchor, endA: Bool) {
        engine.setAnchor(anchor, on: object, endA: endA)
        engine.refreshConstraintPose(object)
        onChange?()
    }

    private func addRing() {
        let ends = engine.endpoints(of: object)
        let center = CGPoint(x: (ends.0.x + ends.1.x) * 0.5, y: (ends.0.y + ends.1.y) * 0.5)
        let ring = engine.createObject(.ring, at: center)
        ring.ringHostRod = object
        object.rodRing = ring
        engine.objects.append(ring)
        onChange?()
        tableView.reloadData()
    }

    private func choosePulleyRamp() {
        let ramps = engine.objects.filter { $0.kind == .straightRamp }
        guard !ramps.isEmpty else { return }
        let alert = UIAlertController(title: "选择直斜面", message: nil, preferredStyle: .actionSheet)
        for ramp in ramps {
            alert.addAction(UIAlertAction(title: "直斜面", style: .default) { [weak self, weak ramp] _ in
                guard let self, let ramp else { return }
                self.object.pulleyRamp = ramp
                self.object.fixed = true
                self.object.position = CGPoint(x: ramp.position.x + (ramp.horizontal ? ramp.size.width * 0.5 : -ramp.size.width * 0.5), y: ramp.position.y - ramp.size.height * 0.5)
                self.object.initialPosition = self.object.position
                self.onChange?()
            })
        }
        alert.addAction(UIAlertAction(title: "取消", style: .cancel))
        alert.popoverPresentationController?.sourceView = view
        alert.popoverPresentationController?.sourceRect = CGRect(x: view.bounds.midX, y: view.bounds.midY, width: 1, height: 1)
        present(alert, animated: true)
    }

    private func chooseLogicTarget() {
        let builder = ConditionBuilderViewController(owner: object, engine: engine)
        builder.onRequestTarget = { [weak self, weak builder] completion in
            guard let self, let builder, let navigation = builder.navigationController else { return }
            navigation.dismiss(animated: true) { [weak self] in
                guard let self else { return }
                self.onSelectConditionTarget?(self.object) { [weak self] target in
                    guard let self else { return }
                    self.present(navigation, animated: true) { completion(target) }
                }
            }
        }
        builder.onComplete = { [weak self] condition in
            self?.dismiss(animated: true) { self?.assignCondition(condition) }
        }
        let navigation = UINavigationController(rootViewController: builder)
        navigation.modalPresentationStyle = .formSheet
        present(navigation, animated: true)
    }

    private func assignCondition(_ condition: LogicCondition) {
        let alert = UIAlertController(title: "应用逻辑到", message: condition.describe(), preferredStyle: .actionSheet)
        [("力-生效", 0), ("速度-生效", 1), ("固定-解除", 2), ("角度固定-解除", 3), ("绑定A-解除", 4), ("绑定B-解除", 5), ("缠绕-解除", 6)].forEach { item in
            alert.addAction(UIAlertAction(title: item.0, style: .default) { [weak self] _ in
                guard let self else { return }
                switch item.1 {
                case 0: self.object.forceCondition = condition
                case 1: self.object.velocityCondition = condition
                case 2: self.object.fixedCondition = condition
                case 3: self.object.angleFixedCondition = condition
                case 4: self.object.bindACondition = condition
                case 5: self.object.bindBCondition = condition
                default: self.object.wrapCondition = condition
                }
                self.onChange?()
            })
        }
        alert.addAction(UIAlertAction(title: "取消", style: .cancel))
        alert.popoverPresentationController?.sourceView = view
        alert.popoverPresentationController?.sourceRect = CGRect(x: view.bounds.midX, y: view.bounds.midY, width: 1, height: 1)
        present(alert, animated: true)
    }

    private func showLogicSummary() {
        var entries: [String] = [
            object.forceCondition.map { "力-生效: \($0.describe())" },
            object.velocityCondition.map { "速度-生效: \($0.describe())" },
            object.fixedCondition.map { "固定-解除: \($0.describe())" },
            object.angleFixedCondition.map { "角度固定-解除: \($0.describe())" },
            object.bindACondition.map { "绑定A-解除: \($0.describe())" },
            object.bindBCondition.map { "绑定B-解除: \($0.describe())" },
            object.wrapCondition.map { "缠绕-解除: \($0.describe())" }
        ].compactMap { $0 }
        for other in engine.objects where other !== object {
            let passive: [(String, LogicCondition?)] = [("\(other.kind.name)力-生效", other.forceCondition), ("\(other.kind.name)速度-生效", other.velocityCondition), ("\(other.kind.name)固定-解除", other.fixedCondition), ("\(other.kind.name)角度固定-解除", other.angleFixedCondition), ("\(other.kind.name)绑定A-解除", other.bindACondition), ("\(other.kind.name)绑定B-解除", other.bindBCondition), ("\(other.kind.name)缠绕-解除", other.wrapCondition)]
            for value in passive where condition(value.1, references: object.id) { entries.append("[被动]\(value.0): \(value.1?.describe() ?? "")") }
        }
        let alert = UIAlertController(title: "逻辑", message: entries.isEmpty ? "无" : entries.joined(separator: "\n"), preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "确定", style: .default))
        present(alert, animated: true)
    }

    private func condition(_ condition: LogicCondition?, references id: UUID) -> Bool {
        guard let condition else { return false }
        return condition.targetID == id || condition.subconditions.contains { self.condition($0, references: id) }
    }

    @objc private func done() { close() }
    @objc private func cancel() { original.restore(object); engine.updateObjectSize(object); onChange?(); close() }
    private func close() { if presentingViewController != nil { dismiss(animated: true) } else { onClose?() } }
}

struct EditableObjectSnapshot {
    let mass: CGFloat
    let friction: CGFloat
    let restitution: CGFloat
    let realWidth: CGFloat
    let realHeight: CGFloat
    let realRadius: CGFloat
    let realLength: CGFloat
    let springConstant: CGFloat
    let elasticLimit: CGFloat
    let baseArea: CGFloat
    let wallThickness: CGFloat
    let waterLevel: CGFloat
    let size: CGSize
    let fixed: Bool
    let remoteLock: Bool
    let showState: Bool
    let showForces: Bool
    let showTrajectory: Bool
    let naturalLength: CGFloat
    let ropeLength: CGFloat
    let horizontal: Bool
    let rotation: CGFloat
    let density: CGFloat
    let rodLength: CGFloat
    let angleFixed: Bool
    let fixedAngle: CGFloat
    let arcAngle: CGFloat
    let pipeMode: Bool
    let pipeDiameter: CGFloat
    let waterEnabled: Bool

    init(_ object: PhysicsObject) {
        mass = object.mass
        friction = object.friction
        restitution = object.restitution
        realWidth = object.realWidth
        realHeight = object.realHeight
        realRadius = object.realRadius
        realLength = object.realLength
        springConstant = object.springConstant
        elasticLimit = object.elasticLimit
        baseArea = object.baseArea
        wallThickness = object.wallThickness
        waterLevel = object.waterLevel
        size = object.size
        fixed = object.fixed
        remoteLock = object.remoteLock
        showState = object.showState
        showForces = object.showForces
        showTrajectory = object.showTrajectory
        naturalLength = object.naturalLength
        ropeLength = object.ropeLength
        horizontal = object.horizontal
        rotation = object.rotation
        density = object.density
        rodLength = object.rodLength
        angleFixed = object.angleFixed
        fixedAngle = object.fixedAngle
        arcAngle = object.arcAngle
        pipeMode = object.pipeMode
        pipeDiameter = object.pipeDiameter
        waterEnabled = object.waterEnabled
    }

    func restore(_ object: PhysicsObject) {
        object.mass = mass
        object.friction = friction
        object.restitution = restitution
        object.realWidth = realWidth
        object.realHeight = realHeight
        object.realRadius = realRadius
        object.realLength = realLength
        object.springConstant = springConstant
        object.elasticLimit = elasticLimit
        object.baseArea = baseArea
        object.wallThickness = wallThickness
        object.waterLevel = waterLevel
        object.size = size
        object.fixed = fixed
        object.remoteLock = remoteLock
        object.showState = showState
        object.showForces = showForces
        object.showTrajectory = showTrajectory
        object.naturalLength = naturalLength
        object.ropeLength = ropeLength
        object.horizontal = horizontal
        object.rotation = rotation
        object.density = density
        object.rodLength = rodLength
        object.angleFixed = angleFixed
        object.fixedAngle = fixedAngle
        object.arcAngle = arcAngle
        object.pipeMode = pipeMode
        object.pipeDiameter = pipeDiameter
        object.waterEnabled = waterEnabled
    }
}

struct NumericProperty {
    let title: String
    let minimum: CGFloat
    let maximum: CGFloat
    let get: () -> CGFloat
    let set: (CGFloat) -> Void
}

struct ToggleProperty {
    let title: String
    let get: () -> Bool
    let set: (Bool) -> Void
}
