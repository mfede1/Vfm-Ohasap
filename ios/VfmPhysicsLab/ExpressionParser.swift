import Foundation

struct ExpressionParser {
    private var characters: [Character] = []
    private var index = 0

    mutating func evaluate(_ input: String) throws -> Double {
        characters = Array(input.replacingOccurrences(of: " ", with: "").replacingOccurrences(of: "π", with: "p"))
        index = 0
        let value = try parseSum()
        if index != characters.count { throw ParseError.invalid }
        return value
    }

    private mutating func parseSum() throws -> Double {
        var value = try parseProduct()
        while let c = peek(), c == "+" || c == "-" {
            index += 1
            let rhs = try parseProduct()
            value = c == "+" ? value + rhs : value - rhs
        }
        return value
    }

    private mutating func parseProduct() throws -> Double {
        var value = try parsePower()
        while let c = peek() {
            if c == "*" || c == "/" || c == "×" || c == "÷" {
                index += 1
                let rhs = try parsePower()
                value = c == "*" || c == "×" ? value * rhs : value / rhs
            } else if startsPrimary(c) {
                value *= try parsePower()
            } else {
                break
            }
        }
        return value
    }

    private mutating func parsePower() throws -> Double {
        var value = try parseUnary()
        if peek() == "^" {
            index += 1
            value = pow(value, try parsePower())
        }
        while peek() == "!" {
            index += 1
            guard value >= 0, value.rounded() == value, value <= 170 else { throw ParseError.invalid }
            value = value == 0 ? 1 : (1...Int(value)).reduce(1) { $0 * Double($1) }
        }
        while peek() == "%" { index += 1; value /= 100 }
        return value
    }

    private mutating func parseUnary() throws -> Double {
        if peek() == "+" { index += 1; return try parseUnary() }
        if peek() == "-" { index += 1; return -(try parseUnary()) }
        if match("sqrt") || match("√") {
            if peek() == "(" { index += 1; let v = try parseSum(); try expect(")"); return sqrt(v) }
            return sqrt(try parseUnary())
        }
        if match("frac") {
            try expect("(")
            let numerator = try parseSum()
            try expect(",")
            let denominator = try parseSum()
            try expect(")")
            return numerator / denominator
        }
        if match("pow") {
            try expect("(")
            let base = try parseSum()
            try expect(",")
            let exponent = try parseSum()
            try expect(")")
            return pow(base, exponent)
        }
        if match("nroot") {
            try expect("(")
            let root = try parseSum()
            try expect(",")
            let value = try parseSum()
            try expect(")")
            return pow(value, 1 / root)
        }
        if match("asin") { return asin(try functionArgument()) }
        if match("acos") { return acos(try functionArgument()) }
        if match("atan") { return atan(try functionArgument()) }
        if match("sin") { return sin(try functionArgument()) }
        if match("cos") { return cos(try functionArgument()) }
        if match("tan") { return tan(try functionArgument()) }
        if match("ln") { return log(try functionArgument()) }
        if match("log") { return log10(try functionArgument()) }
        return try parsePrimary()
    }

    private mutating func functionArgument() throws -> Double {
        try expect("(")
        let value = try parseSum()
        try expect(")")
        return value
    }

    private mutating func parsePrimary() throws -> Double {
        if peek() == "(" {
            index += 1
            let value = try parseSum()
            try expect(")")
            return value
        }
        if peek() == "p" { index += 1; return .pi }
        if peek() == "e" { index += 1; return M_E }
        let start = index
        while let c = peek(), c.isNumber || c == "." { index += 1 }
        guard start != index, let value = Double(String(characters[start..<index])) else { throw ParseError.invalid }
        return value
    }

    private func peek() -> Character? { index < characters.count ? characters[index] : nil }

    private func startsPrimary(_ character: Character) -> Bool {
        character.isNumber || character == "." || character == "(" || character == "p" || character == "e" || character == "√" || character.isLetter
    }

    private mutating func match(_ string: String) -> Bool {
        let target = Array(string)
        guard index + target.count <= characters.count else { return false }
        if Array(characters[index..<(index + target.count)]) == target {
            index += target.count
            return true
        }
        return false
    }

    private mutating func expect(_ character: Character) throws {
        guard peek() == character else { throw ParseError.invalid }
        index += 1
    }

    enum ParseError: Error { case invalid }
}
