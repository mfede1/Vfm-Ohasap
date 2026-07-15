import UIKit

final class PhysicsLabViewController: UIViewController, SimulationViewDelegate {
    let engine = PhysicsEngine()
    lazy var simulationView = SimulationView(engine: engine)
    private let toolStack = UIStackView()
    private let paletteContainer = UIView()
    private let paletteStack = UIStackView()
    private let paletteToggle = UIButton(type: .system)
    private let bottomBar = UIStackView()
    private let playButton = UIButton(type: .system)
    private let clearButton = UIButton(type: .system)
    private let restartButton = UIButton(type: .system)
    private let settingsButton = UIButton(type: .system)
    private let timeSlider = UISlider()
    private let timeLabel = UILabel()
    private let fineCutButton = UIButton(type: .system)
    private let inspectorHost = UIView()
    private weak var inspectorNavigation: UINavigationController?
    private var toolButtons: [UIButton] = []
    private var paletteWidth: NSLayoutConstraint!
    private var paletteOpen = false

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        simulationView.delegate = self
        configureLayout()
        configureTools()
        configurePalette()
        configureBottomBar()
        selectMode(.normal)
    }

    override var prefersStatusBarHidden: Bool { true }
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask { .landscape }

    private func configureLayout() {
        toolStack.axis = .vertical
        toolStack.spacing = 4
        toolStack.alignment = .fill
        toolStack.distribution = .fillEqually
        toolStack.backgroundColor = UIColor(white: 0.94, alpha: 1)
        toolStack.isLayoutMarginsRelativeArrangement = true
        toolStack.layoutMargins = UIEdgeInsets(top: 4, left: 3, bottom: 4, right: 3)
        paletteStack.axis = .vertical
        paletteStack.spacing = 2
        paletteStack.alignment = .fill
        paletteStack.distribution = .fillEqually
        paletteContainer.backgroundColor = UIColor(white: 0.94, alpha: 1)
        paletteContainer.addSubview(paletteStack)
        paletteStack.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            paletteStack.leadingAnchor.constraint(equalTo: paletteContainer.leadingAnchor, constant: 2),
            paletteStack.trailingAnchor.constraint(equalTo: paletteContainer.trailingAnchor, constant: -2),
            paletteStack.topAnchor.constraint(equalTo: paletteContainer.topAnchor, constant: 2),
            paletteStack.bottomAnchor.constraint(equalTo: paletteContainer.bottomAnchor, constant: -2)
        ])
        paletteToggle.setTitle("◀", for: .normal)
        paletteToggle.backgroundColor = UIColor(white: 0.88, alpha: 1)
        paletteToggle.addTarget(self, action: #selector(togglePalette), for: .touchUpInside)
        bottomBar.axis = .horizontal
        bottomBar.spacing = 5
        bottomBar.alignment = .center
        bottomBar.backgroundColor = UIColor(white: 0.91, alpha: 1)
        bottomBar.isLayoutMarginsRelativeArrangement = true
        bottomBar.layoutMargins = UIEdgeInsets(top: 2, left: 8, bottom: 2, right: 8)
        [toolStack, simulationView, paletteToggle, paletteContainer, bottomBar].forEach {
            $0.translatesAutoresizingMaskIntoConstraints = false
            view.addSubview($0)
        }
        fineCutButton.setTitle("□ 精细切割", for: .normal)
        fineCutButton.titleLabel?.font = .systemFont(ofSize: 11)
        fineCutButton.backgroundColor = UIColor.white.withAlphaComponent(0.88)
        fineCutButton.layer.cornerRadius = 4
        fineCutButton.isHidden = true
        fineCutButton.addTarget(self, action: #selector(toggleFineCut), for: .touchUpInside)
        fineCutButton.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(fineCutButton)
        inspectorHost.backgroundColor = .systemBackground
        inspectorHost.layer.shadowColor = UIColor.black.cgColor
        inspectorHost.layer.shadowOpacity = 0.2
        inspectorHost.layer.shadowRadius = 8
        inspectorHost.isHidden = true
        inspectorHost.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(inspectorHost)
        paletteWidth = paletteContainer.widthAnchor.constraint(equalToConstant: 0)
        NSLayoutConstraint.activate([
            toolStack.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor),
            toolStack.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            toolStack.bottomAnchor.constraint(equalTo: bottomBar.topAnchor),
            toolStack.widthAnchor.constraint(equalToConstant: 52),
            simulationView.leadingAnchor.constraint(equalTo: toolStack.trailingAnchor),
            simulationView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            simulationView.bottomAnchor.constraint(equalTo: bottomBar.topAnchor),
            simulationView.trailingAnchor.constraint(equalTo: paletteToggle.leadingAnchor),
            paletteToggle.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            paletteToggle.bottomAnchor.constraint(equalTo: bottomBar.topAnchor),
            paletteToggle.widthAnchor.constraint(equalToConstant: 28),
            paletteContainer.leadingAnchor.constraint(equalTo: paletteToggle.trailingAnchor),
            paletteContainer.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor),
            paletteContainer.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            paletteContainer.bottomAnchor.constraint(equalTo: bottomBar.topAnchor),
            paletteWidth,
            bottomBar.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor),
            bottomBar.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor),
            bottomBar.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),
            bottomBar.heightAnchor.constraint(equalToConstant: 42),
            fineCutButton.leadingAnchor.constraint(equalTo: simulationView.leadingAnchor, constant: 8),
            fineCutButton.topAnchor.constraint(equalTo: simulationView.topAnchor, constant: 8),
            fineCutButton.widthAnchor.constraint(equalToConstant: 86),
            fineCutButton.heightAnchor.constraint(equalToConstant: 30),
            inspectorHost.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor),
            inspectorHost.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            inspectorHost.bottomAnchor.constraint(equalTo: bottomBar.topAnchor),
            inspectorHost.widthAnchor.constraint(equalToConstant: 300)
        ])
    }

    private func configureTools() {
        for mode in ToolMode.allCases {
            let button = UIButton(type: .system)
            button.setTitle(mode.title, for: .normal)
            button.titleLabel?.font = .systemFont(ofSize: mode == .velocity || mode == .force ? 14 : 11, weight: .semibold)
            button.backgroundColor = UIColor(white: 0.8, alpha: 1)
            button.layer.cornerRadius = 4
            button.tag = mode.rawValue
            button.addTarget(self, action: #selector(toolSelected(_:)), for: .touchUpInside)
            toolStack.addArrangedSubview(button)
            toolButtons.append(button)
        }
    }

    private func configurePalette() {
        let kinds = ObjectKind.allCases.filter { $0 != .ring }
        for row in 0..<4 {
            let rowStack = UIStackView()
            rowStack.axis = .horizontal
            rowStack.spacing = 2
            rowStack.distribution = .fillEqually
            for column in 0..<3 {
                let index = row * 3 + column
                if kinds.indices.contains(index) {
                    let button = PaletteButton(kind: kinds[index])
                    button.addTarget(self, action: #selector(paletteTapped(_:)), for: .touchUpInside)
                    let pan = UIPanGestureRecognizer(target: self, action: #selector(paletteDragged(_:)))
                    button.addGestureRecognizer(pan)
                    rowStack.addArrangedSubview(button)
                } else {
                    rowStack.addArrangedSubview(UIView())
                }
            }
            paletteStack.addArrangedSubview(rowStack)
        }
    }

    private func configureBottomBar() {
        settingsButton.setTitle("⚙", for: .normal)
        settingsButton.addTarget(self, action: #selector(showGlobalSettings), for: .touchUpInside)
        playButton.setTitle("▶ 播放", for: .normal)
        playButton.addTarget(self, action: #selector(togglePlayback), for: .touchUpInside)
        clearButton.setTitle("清空", for: .normal)
        clearButton.addTarget(self, action: #selector(clearScene), for: .touchUpInside)
        restartButton.setTitle("重计", for: .normal)
        restartButton.addTarget(self, action: #selector(restartScene), for: .touchUpInside)
        timeSlider.minimumValue = 0
        timeSlider.maximumValue = 60
        timeSlider.value = 0
        timeSlider.addTarget(self, action: #selector(timeChanged(_:)), for: .valueChanged)
        timeLabel.text = "0.0 s"
        timeLabel.font = .monospacedDigitSystemFont(ofSize: 11, weight: .regular)
        timeLabel.textAlignment = .center
        timeLabel.isUserInteractionEnabled = true
        timeLabel.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(jumpToTime)))
        timeLabel.widthAnchor.constraint(equalToConstant: 48).isActive = true
        timeSlider.setContentHuggingPriority(.defaultLow, for: .horizontal)
        [settingsButton, playButton, clearButton, restartButton].forEach { $0.heightAnchor.constraint(equalToConstant: 34).isActive = true }
        bottomBar.addArrangedSubview(settingsButton)
        bottomBar.addArrangedSubview(playButton)
        bottomBar.addArrangedSubview(clearButton)
        bottomBar.addArrangedSubview(restartButton)
        bottomBar.addArrangedSubview(timeSlider)
        bottomBar.addArrangedSubview(timeLabel)
    }

    @objc private func toolSelected(_ sender: UIButton) {
        guard let mode = ToolMode(rawValue: sender.tag) else { return }
        selectMode(mode)
    }

    private func selectMode(_ mode: ToolMode) {
        simulationView.mode = mode
        fineCutButton.isHidden = mode != .cut
        let colors: [UIColor] = [.systemGreen, .systemBlue, .systemOrange, .systemPurple, .systemRed, .systemBrown]
        for button in toolButtons {
            let active = button.tag == mode.rawValue
            button.backgroundColor = active ? colors[button.tag] : UIColor(white: 0.8, alpha: 1)
            button.setTitleColor(active ? .white : .black, for: .normal)
        }
    }

    @objc private func toggleFineCut() {
        simulationView.fineCutEnabled.toggle()
        fineCutButton.setTitle(simulationView.fineCutEnabled ? "☑ 精细切割" : "□ 精细切割", for: .normal)
    }

    @objc private func togglePalette() {
        paletteOpen.toggle()
        paletteWidth.constant = paletteOpen ? 210 : 0
        paletteToggle.setTitle(paletteOpen ? "▶" : "◀", for: .normal)
        UIView.animate(withDuration: 0.2) { self.view.layoutIfNeeded() }
    }

    @objc private func paletteTapped(_ sender: PaletteButton) {
        let point = CGPoint(x: simulationView.bounds.midX, y: simulationView.bounds.midY)
        let object = engine.add(sender.kind, at: point)
        simulationView.selectedObject = object
        simulationView.setNeedsDisplay()
    }

    @objc private func paletteDragged(_ recognizer: UIPanGestureRecognizer) {
        guard let source = recognizer.view as? PaletteButton else { return }
        let point = recognizer.location(in: simulationView)
        switch recognizer.state {
        case .began, .changed:
            simulationView.showPalettePreview(kind: source.kind, at: point)
        case .ended:
            simulationView.showPalettePreview(kind: nil, at: point)
            if simulationView.bounds.contains(point) {
                let object = engine.add(source.kind, at: point)
                simulationView.selectedObject = object
            }
            simulationView.setNeedsDisplay()
        default:
            simulationView.showPalettePreview(kind: nil, at: point)
        }
    }

    @objc private func togglePlayback() {
        simulationView.isRunning.toggle()
        playButton.setTitle(simulationView.isRunning ? "⏸ 暂停" : "▶ 播放", for: .normal)
        if simulationView.isRunning { engine.savePlaybackBaseline() }
    }

    @objc private func clearScene() {
        simulationView.isRunning = false
        playButton.setTitle("▶ 播放", for: .normal)
        engine.reset(clear: true)
        simulationView.selectedObject = nil
        updateTimeline()
        simulationView.setNeedsDisplay()
    }

    @objc private func restartScene() {
        simulationView.isRunning = false
        playButton.setTitle("▶ 播放", for: .normal)
        engine.elapsedTime = 0
        updateTimeline()
        simulationView.setNeedsDisplay()
    }

    @objc private func timeChanged(_ slider: UISlider) {
        simulationView.isRunning = false
        playButton.setTitle("▶ 播放", for: .normal)
        let target = CGFloat(slider.value)
        engine.restore(at: target)
        updateTimeline()
        simulationView.setNeedsDisplay()
    }

    @objc private func jumpToTime() {
        promptExpression(title: "跳转时间 (秒)", current: String(format: "%.1f", Double(engine.elapsedTime))) { [weak self] value in
            guard let self else { return }
            self.simulationView.isRunning = false
            let target = max(0, min(60, value))
            self.engine.restore(at: target)
            self.updateTimeline()
            self.simulationView.setNeedsDisplay()
        }
    }

    @objc private func showGlobalSettings() {
        let controller = GlobalSettingsViewController(engine: engine)
        controller.onChange = { [weak self] in
            self?.simulationView.setNeedsDisplay()
        }
        let navigation = UINavigationController(rootViewController: controller)
        navigation.modalPresentationStyle = .formSheet
        present(navigation, animated: true)
    }

    func simulationView(_ view: SimulationView, selected object: PhysicsObject?) { if object == nil { closeInspector() } }

    func simulationViewDidChange(_ view: SimulationView) {
        updateTimeline()
    }

    func simulationView(_ view: SimulationView, requestInspector object: PhysicsObject) {
        closeInspector()
        let controller = ObjectInspectorViewController(object: object, engine: engine)
        controller.onChange = { [weak self] in self?.simulationView.setNeedsDisplay() }
        controller.onDelete = { [weak self] in
            self?.simulationView.selectedObject = nil
            self?.simulationView.setNeedsDisplay()
        }
        controller.onWrapDrawing = { [weak self] rope in
            self?.closeInspector()
            self?.simulationView.beginWrapDrawing(rope)
        }
        controller.onClose = { [weak self] in self?.closeInspector() }
        controller.onConnect = { [weak self] link, endA in
            self?.closeInspector()
            self?.simulationView.beginConnection(link, endA: endA)
        }
        controller.onSelectPulleyRamp = { [weak self] pulley in
            self?.closeInspector()
            self?.simulationView.beginPulleyRampSelection(pulley)
        }
        controller.onRotate = { [weak self] object in
            self?.closeInspector()
            self?.simulationView.beginRotation(object)
        }
        controller.onSelectConditionTarget = { [weak self] owner, completion in
            guard let self else { return }
            self.inspectorHost.isHidden = true
            self.simulationView.beginConditionTargetSelection(owner: owner) { [weak self] target in
                guard let self else { return }
                self.inspectorHost.isHidden = false
                completion(target)
            }
        }
        let navigation = UINavigationController(rootViewController: controller)
        addChild(navigation)
        navigation.view.frame = inspectorHost.bounds
        navigation.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        inspectorHost.addSubview(navigation.view)
        navigation.didMove(toParent: self)
        inspectorNavigation = navigation
        inspectorHost.isHidden = false
    }

    private func closeInspector() {
        guard let navigation = inspectorNavigation else { inspectorHost.isHidden = true; return }
        navigation.willMove(toParent: nil)
        navigation.view.removeFromSuperview()
        navigation.removeFromParent()
        inspectorHost.isHidden = true
    }

    func simulationView(_ view: SimulationView, requestFineCut object: PhysicsObject) {
        let controller = FineCutViewController(object: object, engine: engine)
        controller.onApply = { [weak self] in self?.simulationView.setNeedsDisplay() }
        let navigation = UINavigationController(rootViewController: controller)
        navigation.modalPresentationStyle = .formSheet
        present(navigation, animated: true)
    }

    func simulationView(_ view: SimulationView, requestVectorFor object: PhysicsObject, force: Bool, magnitude: CGFloat, angle: CGFloat) {
        let controller = VectorEditorViewController(object: object, engine: engine, force: force, magnitude: magnitude, angle: angle)
        controller.onApply = { [weak self] in self?.simulationView.setNeedsDisplay() }
        controller.onSelectConditionTarget = { [weak self] owner, completion in
            self?.simulationView.beginConditionTargetSelection(owner: owner, completion: completion)
        }
        let navigation = UINavigationController(rootViewController: controller)
        navigation.modalPresentationStyle = .formSheet
        present(navigation, animated: true)
    }

    private func updateTimeline() {
        timeSlider.value = Float(engine.elapsedTime)
        timeLabel.text = String(format: "%.1f s", Double(engine.elapsedTime))
        if !simulationView.isRunning { playButton.setTitle("▶ 播放", for: .normal) }
    }
}

final class PaletteButton: UIButton {
    let kind: ObjectKind

    init(kind: ObjectKind) {
        self.kind = kind
        super.init(frame: .zero)
        setTitle(kind.name, for: .normal)
        titleLabel?.font = .systemFont(ofSize: 11)
        setTitleColor(.label, for: .normal)
        backgroundColor = .white
        layer.borderWidth = 0.5
        layer.borderColor = UIColor.lightGray.cgColor
        layer.cornerRadius = 3
    }

    required init?(coder: NSCoder) { nil }

    override func layoutSubviews() {
        super.layoutSubviews()
        titleLabel?.frame = CGRect(x: 2, y: bounds.height - 24, width: bounds.width - 4, height: 20)
        titleLabel?.textAlignment = .center
    }

    override func draw(_ rect: CGRect) {
        super.draw(rect)
        guard let context = UIGraphicsGetCurrentContext() else { return }
        let center = CGPoint(x: bounds.midX, y: bounds.midY - 10)
        context.setStrokeColor(UIColor.label.cgColor)
        context.setFillColor(UIColor.white.cgColor)
        context.setLineWidth(1.5)
        switch kind {
        case .block:
            context.beginPath()
            context.addRect(CGRect(x: center.x - 18, y: center.y - 9, width: 36, height: 18))
            context.drawPath(using: .fillStroke)
        case .board:
            context.beginPath()
            context.addRect(CGRect(x: center.x - 22, y: center.y - 3, width: 44, height: 6))
            context.drawPath(using: .fillStroke)
        case .spring:
            context.move(to: CGPoint(x: center.x - 22, y: center.y))
            for index in 0..<4 {
                let x = center.x - 22 + CGFloat(index) * 11
                context.addLine(to: CGPoint(x: x + 3, y: center.y - 5))
                context.addLine(to: CGPoint(x: x + 8, y: center.y + 5))
                context.addLine(to: CGPoint(x: x + 11, y: center.y))
            }
            context.strokePath()
        case .rope, .rod:
            context.move(to: CGPoint(x: center.x - 22, y: center.y))
            context.addLine(to: CGPoint(x: center.x + 22, y: center.y))
            context.strokePath()
            context.fillEllipse(in: CGRect(x: center.x - 24, y: center.y - 2, width: 4, height: 4))
            context.fillEllipse(in: CGRect(x: center.x + 20, y: center.y - 2, width: 4, height: 4))
        case .straightRamp:
            context.move(to: CGPoint(x: center.x - 20, y: center.y + 10))
            context.addLine(to: CGPoint(x: center.x + 20, y: center.y + 10))
            context.addLine(to: CGPoint(x: center.x + 20, y: center.y - 10))
            context.closePath()
            context.drawPath(using: .fillStroke)
        case .arcRamp:
            context.move(to: CGPoint(x: center.x - 18, y: center.y + 10))
            context.addLine(to: CGPoint(x: center.x + 18, y: center.y + 10))
            context.addLine(to: CGPoint(x: center.x + 18, y: center.y - 10))
            context.addQuadCurve(to: CGPoint(x: center.x - 18, y: center.y + 10), control: CGPoint(x: center.x - 12, y: center.y - 8))
            context.closePath()
            context.drawPath(using: .fillStroke)
        case .ball:
            context.setFillColor(UIColor.orange.cgColor)
            context.fillEllipse(in: CGRect(x: center.x - 11, y: center.y - 11, width: 22, height: 22))
            context.strokeEllipse(in: CGRect(x: center.x - 11, y: center.y - 11, width: 22, height: 22))
        case .container:
            context.move(to: CGPoint(x: center.x - 17, y: center.y - 10))
            context.addLine(to: CGPoint(x: center.x - 17, y: center.y + 11))
            context.addLine(to: CGPoint(x: center.x + 17, y: center.y + 11))
            context.addLine(to: CGPoint(x: center.x + 17, y: center.y - 10))
            context.strokePath()
            context.setFillColor(UIColor.systemCyan.withAlphaComponent(0.7).cgColor)
            context.fill(CGRect(x: center.x - 15, y: center.y + 1, width: 30, height: 8))
        case .pulley:
            context.strokeEllipse(in: CGRect(x: center.x - 12, y: center.y - 12, width: 24, height: 24))
            context.strokeEllipse(in: CGRect(x: center.x - 7, y: center.y - 7, width: 14, height: 14))
        case .ring:
            context.setLineWidth(3)
            context.strokeEllipse(in: CGRect(x: center.x - 10, y: center.y - 10, width: 20, height: 20))
        }
    }
}

final class GlobalSettingsViewController: UITableViewController {
    let engine: PhysicsEngine
    var onChange: (() -> Void)?
    private let rows = ["地面长度(m)", "风力(N)", "风向(°)", "空气阻力(N)", "小数位数"]
    private let ranges: [(CGFloat, CGFloat)] = [(0.1, 50), (0, 20), (0, 360), (0, 20), (0, 15)]

    init(engine: PhysicsEngine) {
        self.engine = engine
        super.init(style: .insetGrouped)
        title = "全局设置"
    }

    required init?(coder: NSCoder) { nil }

    override func viewDidLoad() {
        super.viewDidLoad()
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "确定", style: .done, target: self, action: #selector(done))
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { rows.count }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = UITableViewCell(style: .value1, reuseIdentifier: nil)
        cell.textLabel?.text = rows[indexPath.row]
        let accessory = UIView(frame: CGRect(x: 0, y: 0, width: 150, height: 34))
        let slider = UISlider(frame: CGRect(x: 0, y: 2, width: 98, height: 30))
        slider.minimumValue = Float(ranges[indexPath.row].0)
        slider.maximumValue = Float(ranges[indexPath.row].1)
        slider.value = Float(numericValue(at: indexPath.row))
        slider.tag = indexPath.row
        slider.addTarget(self, action: #selector(sliderChanged(_:)), for: .valueChanged)
        slider.addTarget(self, action: #selector(sliderFinished(_:)), for: [.touchUpInside, .touchUpOutside, .touchCancel])
        let label = UILabel(frame: CGRect(x: 102, y: 0, width: 48, height: 34))
        label.tag = 999
        label.font = .monospacedDigitSystemFont(ofSize: 9, weight: .regular)
        label.textAlignment = .right
        label.adjustsFontSizeToFitWidth = true
        label.text = value(at: indexPath.row)
        accessory.addSubview(slider)
        accessory.addSubview(label)
        cell.accessoryView = accessory
        return cell
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        promptExpression(title: rows[indexPath.row], current: value(at: indexPath.row)) { [weak self] value in
            guard let self else { return }
            let bounded = max(self.ranges[indexPath.row].0, min(self.ranges[indexPath.row].1, value))
            self.apply(bounded, at: indexPath.row)
            self.tableView.reloadRows(at: [indexPath], with: .none)
            self.onChange?()
        }
    }

    @objc private func sliderChanged(_ sender: UISlider) {
        var value = CGFloat(sender.value)
        if sender.tag == 4 { value = CGFloat(Int(value)) }
        apply(value, at: sender.tag)
        (sender.superview?.viewWithTag(999) as? UILabel)?.text = self.value(at: sender.tag)
        onChange?()
    }

    @objc private func sliderFinished(_ sender: UISlider) {
        tableView.reloadData()
    }

    private func apply(_ value: CGFloat, at row: Int) {
        switch row {
        case 0: engine.groundLength = value; engine.pixelsPerMeter = max(1, engine.wallRightX / value); engine.updateObjectSizes()
        case 1: engine.windForce = value
        case 2: engine.windDirection = value
        case 3: engine.airResistance = value
        default: engine.decimalPlaces = Int(value)
        }
    }

    private func numericValue(at row: Int) -> CGFloat {
        switch row {
        case 0: return engine.groundLength
        case 1: return engine.windForce
        case 2: return engine.windDirection
        case 3: return engine.airResistance
        default: return CGFloat(engine.decimalPlaces)
        }
    }

    private func value(at row: Int) -> String {
        switch row {
        case 0: return format(engine.groundLength)
        case 1: return format(engine.windForce)
        case 2: return format(engine.windDirection)
        case 3: return format(engine.airResistance)
        default: return String(engine.decimalPlaces)
        }
    }

    private func format(_ value: CGFloat) -> String { String(format: "%.3g", Double(value)) }
    @objc private func done() { dismiss(animated: true) }
}

extension UIViewController {
    func promptExpression(title: String, current: String, completion: @escaping (CGFloat) -> Void) {
        let input = FormulaInputViewController(title: title, initial: current)
        input.onConfirm = { text in
            var parser = ExpressionParser()
            if let value = try? parser.evaluate(text), value.isFinite { completion(CGFloat(value)) }
        }
        let navigation = UINavigationController(rootViewController: input)
        navigation.modalPresentationStyle = .formSheet
        present(navigation, animated: true)
    }
}
