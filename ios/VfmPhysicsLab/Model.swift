import UIKit

enum ObjectKind: Int, CaseIterable {
    case block
    case board
    case spring
    case rope
    case straightRamp
    case arcRamp
    case ball
    case container
    case pulley
    case rod
    case ring

    var name: String {
        switch self {
        case .block: return "木块"
        case .board: return "木板"
        case .spring: return "弹簧"
        case .rope: return "绳子"
        case .straightRamp: return "直斜面"
        case .arcRamp: return "弧形斜面"
        case .ball: return "小球"
        case .container: return "容器"
        case .pulley: return "滑轮"
        case .rod: return "杆"
        case .ring: return "环"
        }
    }
}

enum ToolMode: Int, CaseIterable {
    case normal
    case velocity
    case force
    case draw
    case cut
    case erase

    var title: String {
        switch self {
        case .normal: return "普通"
        case .velocity: return "v₀"
        case .force: return "a"
        case .draw: return "绘制"
        case .cut: return "切割"
        case .erase: return "擦除"
        }
    }
}

struct CeilingSegment {
    var start: CGPoint
    var end: CGPoint
    var length: CGFloat { hypot(end.x - start.x, end.y - start.y) }
}

struct RopeParticle {
    var position: CGPoint
    var velocity: CGVector
}

enum Anchor: Equatable {
    case none
    case object(UUID)
    case ceiling(Int, CGFloat)
    case ground(CGFloat)
    case leftWall(CGFloat)
    case rightWall(CGFloat)
}

enum ConditionKind: Int, CaseIterable {
    case afterTime = 1
    case contactObject
    case timerReach
    case objectMissing
    case contactDuration
    case contactTotal
    case afterNthContact
    case compound
}

final class LogicCondition {
    var kind: ConditionKind = .afterTime
    var time: CGFloat = 1
    var requiredCount = -1
    var targetID: UUID?
    var subconditions: [LogicCondition] = []
    var totalTimer: CGFloat = 0
    var contactStarted = false
    var triggered = false
    var contactNth = 1
    var contactCount = 0
    var startTime: CGFloat = -1
    var totalContactStart: CGFloat = -1

    func reset() {
        totalTimer = 0
        contactStarted = false
        triggered = false
        contactCount = 0
        startTime = -1
        totalContactStart = -1
        subconditions.forEach { $0.reset() }
    }

    func describe() -> String {
        switch kind {
        case .afterTime: return "经过\(formatted(time))s"
        case .contactObject: return "接触物体"
        case .timerReach: return "至\(formatted(time))s"
        case .objectMissing: return "物体不存在"
        case .contactDuration: return "连续接触物体\(formatted(time))s"
        case .contactTotal: return "接触物体总计\(formatted(time))s"
        case .afterNthContact: return "第\(contactNth == -1 ? "最后" : String(contactNth))次接触物体\(formatted(time))s后"
        case .compound:
            let need = requiredCount > 0 ? requiredCount : subconditions.count
            return "复合逻辑(需\(need)个)[\(subconditions.map { $0.describe() }.joined(separator: ","))]"
        }
    }

    private func formatted(_ value: CGFloat) -> String {
        String(format: "%.2g", Double(value))
    }
}

final class PhysicsObject: Hashable {
    let id: UUID
    var kind: ObjectKind
    var position: CGPoint
    var size: CGSize
    var velocity: CGVector = .zero
    var force: CGVector = .zero
    var appliedForce: CGVector = .zero
    var displayForce: CGVector = .zero
    var constraintForce: CGVector = .zero
    var supportForce: CGVector = .zero
    var frictionForce: CGVector = .zero
    var reactionForce: CGVector = .zero
    var acceleration: CGVector = .zero
    var previousPosition: CGPoint = .zero
    var previousVelocity: CGVector = .zero
    var previousContactRamp = false
    var stepMechanicalEnergy: CGFloat = 0
    var rotation: CGFloat = 0
    var mass: CGFloat = 1
    var friction: CGFloat = 0.3
    var restitution: CGFloat = 0.3
    var fixed = false
    var showState = false
    var showForces = false
    var showTrajectory = false
    var naturalLength: CGFloat = 80
    var springConstant: CGFloat = 500
    var ropeLength: CGFloat = 100
    var elasticLimit: CGFloat = 200
    var realWidth: CGFloat = 0.6
    var realHeight: CGFloat = 0.4
    var realRadius: CGFloat = 0.2
    var realLength: CGFloat = 1
    var initialPosition: CGPoint
    var initialVelocity: CGVector = .zero
    var horizontal = true
    var deformation: CGFloat = 0
    var forceDuration: CGFloat = -1
    var forceTimer: CGFloat = 0
    var isCutHalf = false
    var cutHalfAnchor = CGPoint.zero
    var cutHalfFree = CGPoint.zero
    weak var cutHalfFollowObject: PhysicsObject?
    var cutHalfLength: CGFloat = 0
    var cutDecaying = false
    var cutDecayTimer: CGFloat = 0
    var polygon: [CGPoint] = []
    var trajectory: [CGPoint] = []
    var velocityHistory: [CGFloat] = []
    var positionHistory: [CGFloat] = []
    var accelerationHistory: [CGFloat] = []
    var contactGround = false
    var contactLeftWall = false
    var contactRightWall = false
    var contactRamp = false
    weak var rampContact: PhysicsObject?
    var anchorA: Anchor = .none
    var anchorB: Anchor = .none
    var freeEnd: CGPoint
    var freeEndVelocity: CGVector = .zero
    var freeAppliedForce: CGVector = .zero
    var rodLength: CGFloat = 100
    var remoteLock = false
    weak var rodRing: PhysicsObject?
    weak var ringHostRod: PhysicsObject?
    var ringParameter: CGFloat = 0.5
    var pressure: CGFloat = 0
    var wallThickness: CGFloat = 0.02
    var waterLevel: CGFloat = 0
    var waterEnabled = false
    weak var pulleyRamp: PhysicsObject?
    var ropeWrapped = false
    var wrapPoints: [CGPoint] = []
    var ropeParticles: [RopeParticle] = []
    var ropeParticlesInitialized = false
    var angleFixed = false
    var fixedAngle: CGFloat = 0
    var arcAngle: CGFloat = .pi
    var pipeMode = false
    var pipeDiameter: CGFloat = 0.3
    var density: CGFloat = 500
    var baseArea: CGFloat = 0.06
    var forceCondition: LogicCondition?
    var velocityCondition: LogicCondition?
    var bindACondition: LogicCondition?
    var bindBCondition: LogicCondition?
    var wrapCondition: LogicCondition?
    var fixedCondition: LogicCondition?
    var angleFixedCondition: LogicCondition?
    var conserveCircleHorizontal = false
    var circleMomentumInitialized = false
    var circleSystemCenterX: CGFloat = 0
    var circleSystemMomentumX: CGFloat = 0
    var circleSystemMass: CGFloat = 0
    var circleSystemMemberIDs: [UUID] = []

    init(id: UUID = UUID(), kind: ObjectKind, position: CGPoint, size: CGSize) {
        self.id = id
        self.kind = kind
        self.position = position
        self.initialPosition = position
        self.previousPosition = position
        self.size = size
        self.freeEnd = position
    }

    static func == (lhs: PhysicsObject, rhs: PhysicsObject) -> Bool { lhs === rhs }
    func hash(into hasher: inout Hasher) { hasher.combine(ObjectIdentifier(self)) }

    var radius: CGFloat { max(size.width, size.height) * 0.5 }
    var bounds: CGRect { CGRect(x: position.x - size.width * 0.5, y: position.y - size.height * 0.5, width: size.width, height: size.height) }
    var speed: CGFloat { hypot(velocity.dx, velocity.dy) }
}

struct ObjectState {
    var position: CGPoint
    var velocity: CGVector
    var rotation: CGFloat
    var freeEnd: CGPoint
    var freeEndVelocity: CGVector
}

struct SimulationState {
    var time: CGFloat
    var states: [UUID: ObjectState]
}

extension CGPoint {
    static func + (lhs: CGPoint, rhs: CGVector) -> CGPoint { CGPoint(x: lhs.x + rhs.dx, y: lhs.y + rhs.dy) }
    static func - (lhs: CGPoint, rhs: CGPoint) -> CGVector { CGVector(dx: lhs.x - rhs.x, dy: lhs.y - rhs.y) }
    static func - (lhs: CGPoint, rhs: CGVector) -> CGPoint { CGPoint(x: lhs.x - rhs.dx, y: lhs.y - rhs.dy) }
}

extension CGVector {
    static func + (lhs: CGVector, rhs: CGVector) -> CGVector { CGVector(dx: lhs.dx + rhs.dx, dy: lhs.dy + rhs.dy) }
    static func - (lhs: CGVector, rhs: CGVector) -> CGVector { CGVector(dx: lhs.dx - rhs.dx, dy: lhs.dy - rhs.dy) }
    static func * (lhs: CGVector, rhs: CGFloat) -> CGVector { CGVector(dx: lhs.dx * rhs, dy: lhs.dy * rhs) }
    static func / (lhs: CGVector, rhs: CGFloat) -> CGVector { CGVector(dx: lhs.dx / rhs, dy: lhs.dy / rhs) }
    var length: CGFloat { hypot(dx, dy) }
    var normalized: CGVector { length > 0.000001 ? self / length : .zero }
    func dot(_ other: CGVector) -> CGFloat { dx * other.dx + dy * other.dy }
}
