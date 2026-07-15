import Foundation

enum SurdFormatter {
    static func text(_ value: CGFloat, places: Int) -> String {
        let number = Double(value)
        if number.isNaN { return "NaN" }
        if number.isInfinite { return number > 0 ? "∞" : "-∞" }
        if number == 0 { return "0" }
        let negative = number < 0
        let absolute = abs(number)
        if absolute.rounded(.down) == absolute, absolute < 1e15 { return String(Int64(number)) }
        if let fraction = fraction(absolute) {
            let sign = negative ? "-" : ""
            return fraction.1 == 1 ? sign + String(fraction.0) : sign + "\(fraction.0)/\(fraction.1)"
        }
        if let surd = surd(absolute) { return negative ? "-" + surd : surd }
        if places <= 0 { return String(Int(number.rounded())) }
        var result = String(format: "%.*f", places, number)
        while result.last == "0" { result.removeLast() }
        if result.last == "." { result.removeLast() }
        return result
    }

    private static func fraction(_ value: Double) -> (Int64, Int64)? {
        guard value <= 1000 else { return nil }
        for denominator in 1...1000 {
            let numerator = Int64((value * Double(denominator)).rounded())
            if numerator == 0 { continue }
            if abs(value - Double(numerator) / Double(denominator)) < 1e-9 {
                let divisor = gcd(numerator, Int64(denominator))
                return (numerator / divisor, Int64(denominator) / divisor)
            }
        }
        return nil
    }

    private static func surd(_ value: Double) -> String? {
        guard value > 1e-9, value <= 1000 else { return nil }
        let square = value * value
        let rounded = Int64(square.rounded())
        if abs(square - Double(rounded)) < 1e-6, rounded > 0, rounded < 100000 { return simplifyRoot(rounded) }
        for denominator in 2...20 {
            let scaled = value * Double(denominator)
            let scaledSquare = scaled * scaled
            let inner = Int64(scaledSquare.rounded())
            if abs(scaledSquare - Double(inner)) < 1e-6, let parts = rootParts(inner), parts.1 > 1 {
                let divisor = gcd(parts.0, Int64(denominator))
                let coefficient = parts.0 / divisor
                let reducedDenominator = Int64(denominator) / divisor
                let root = coefficient == 1 ? "√\(parts.1)" : "\(coefficient)√\(parts.1)"
                return reducedDenominator == 1 ? root : "\(root)/\(reducedDenominator)"
            }
        }
        for coefficient in 2...20 {
            let scaled = value / Double(coefficient)
            let inner = Int64((scaled * scaled).rounded())
            if abs(scaled * scaled - Double(inner)) < 1e-6, let root = simplifyRoot(inner), root.contains("√") {
                return root.hasPrefix("√") ? "\(coefficient)\(root)" : "\(coefficient)*\(root)"
            }
        }
        return nil
    }

    private static func simplifyRoot(_ value: Int64) -> String? {
        guard let parts = rootParts(value) else { return nil }
        if parts.1 == 1 { return String(parts.0) }
        return parts.0 == 1 ? "√\(parts.1)" : "\(parts.0)√\(parts.1)"
    }

    private static func rootParts(_ value: Int64) -> (Int64, Int64)? {
        guard value > 0 else { return nil }
        if value == 1 { return (1, 1) }
        var outer: Int64 = 1
        var inner = value
        var factor: Int64 = 2
        while factor * factor <= inner {
            while inner % (factor * factor) == 0 {
                outer *= factor
                inner /= factor * factor
            }
            factor += 1
        }
        return (outer, inner)
    }

    private static func gcd(_ first: Int64, _ second: Int64) -> Int64 {
        var a = first
        var b = second
        while b != 0 { (a, b) = (b, a % b) }
        return abs(a)
    }
}
