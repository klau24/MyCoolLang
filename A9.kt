//======================== Data Definitions ================================
//ExprC definition
open class ExprC()
class NumC(val n: Double) : ExprC() {
    override fun equals(other: Any?)
            = (other is NumC)
            && this.n == other.n
}
class IdC(val s: String) : ExprC() {
    override fun equals(other: Any?)
            = (other is IdC)
            && this.s == other.s
}
class StrC(val s: String) : ExprC() {
    override fun equals(other: Any?)
            = (other is StrC)
            && this.s == other.s
}
class IfC(val test: ExprC, val thn: ExprC, val els: ExprC) : ExprC() {
    override fun equals(other: Any?)
            = (other is IfC)
            && this.test == other.test
            && this.thn == other.thn
            && this.els == other.els
}
class LamC(val args: ArrayList<String>, val body: ExprC) : ExprC() {
    override fun equals(other: Any?)
            = (other is LamC)
            && this.args == other.args
            && this.body == other.body
}
class AppC(val name: ExprC, val args: List<ExprC>) : ExprC() {
    override fun equals(other: Any?)
            = (other is AppC)
            && this.name == other.name
            && this.args == this.args
}

//Binding
class Binding(val name: String, val the_value: Value) {
    override fun equals(other: Any?)
            = (other is Binding)
            && this.name == other.name
            && this.the_value == other.the_value
}

//Env
class Env(val b: ArrayList<Binding>) {
    override fun equals(other: Any?)
            = (other is Env)
            && this.b == other.b
}

// Value definition
open class Value
class NumV(val n: Double) : Value() {
    override fun equals(other: Any?)
            = (other is NumV)
            && this.n == other.n
    override fun toString(): String
            = this.n.toString()
}
class BoolV(val b: Boolean) : Value()  {
    override fun equals(other: Any?)
            = (other is BoolV)
            && this.b == other.b
}
class StrV(val s: String) : Value()  {
    override fun equals(other: Any?)
            = (other is StrV)
            && this.s == other.s
}
class CloV(val param: ArrayList<String>, val body: ExprC, val env: Env) : Value()  {
    override fun equals(other: Any?)
            = (other is CloV)
            && this.param == other.param
            && this.body == other.body
            && this.env == other.env
}
class PrimV(val p: String) : Value()  {
    override fun equals(other: Any?)
            = (other is PrimV)
            && this.p == other.p
}

// Reserved Id's
val reserved = arrayOf<String>("local", "in", "if", "lam")

//======================== INTERPRETING ================================

// top-interp (main): given a Sexp return the string representation
fun main(args: Array<String>) {
    val topEnv = arrayListOf<Binding>(
        Binding("+", PrimV("+")),
        Binding("-", PrimV("-")),
        Binding("*", PrimV("*")),
        Binding("/", PrimV("/")),
        Binding("<=", PrimV("<=")),
        Binding("equal?", PrimV("equal?")),
        Binding("true", BoolV(true)),
        Binding("false", BoolV(false)),
        Binding("error", PrimV("error")),
        Binding("substring", PrimV("substring"))
    )

    // Reserved Id's
    val reserved = arrayOf<String>("local", "in", "if", "lam")


}

// serialize: given a Value return its string representation
fun serialize(v: Value): String {
    return when(v) {
        is NumV -> v.n.toString()
        is BoolV -> if(v.b) "true" else "false"
        is StrV -> v.s
        is CloV -> "#<procedure>"
        is PrimV -> "#<primop>"
        else -> throw Exception("cannot match input value, $v")
    }
}

// interp: Interprets the given expression, using the list of funs to resolve applications.
fun interp(exp: ExprC, env: Env): Value {
    return when(exp) {
        is NumC -> NumV(exp.n)
        is IdC -> (envLookup(exp.s, env))
        is StrC -> StrV(exp.s)
        is LamC -> CloV(exp.args, exp.body, env)
        is IfC -> when(val test = interp(exp.test, env)) {
                        is BoolV -> if (test.b) interp(exp.thn, env)
                                    else interp(exp.els, env)
                        else -> throw Exception("If test not evaluating to a boolean $exp")
        }
        is AppC -> when(val app = interp(exp.name, env)) {
                        is CloV -> if ((app.param).size == (exp.args).size) {
                                        val argval = (exp.args).map {arg -> interp(arg, env)};
                                        val newEnv = makeBindings(app.param, (argval as ArrayList<Value>), app.env);
                                        interp(app.body, newEnv);
                                    } else {
                                        throw Exception("Args wrong arity $app.param")
                                    }
                        is PrimV -> {
                            val v = (exp.args).map {arg -> interp(arg, env)};
                            primInterp(app, (v as ArrayList<Value>));
                        }
                        else -> throw Exception("Not a function $exp.name")
        }
        else -> throw Exception("Interp did not receive an ExprC $exp")
    }
}

// prim-interp: takes in a primitive and values, returns the value of the resulting operation
fun primInterp(p: PrimV, vals: ArrayList<Value>): Value {
    return when(vals.size) {
        0 -> throw Exception("PrimV has no values $p")
        1 -> when(p.p) {
                    "error" -> throw Exception("User error $vals")
                    else -> throw Exception("PrimV: $p does not have enough vals: $vals")
        }
        2 -> when {
            (vals[0] is NumV) && (vals[1] is NumV) -> {val a = (vals[0] as NumV).n;
                                                        val b = (vals[1] as NumV).n;
                                                        when(p.p) {
                                                            "+" -> NumV(a + b)
                                                            "-" -> NumV(a - b)
                                                            "*" -> NumV(a * b)
                                                            "/" -> NumV(a / b)
                                                            "<=" -> BoolV(a <= b)
                                                            "equal?" -> BoolV(a == b)
                                                            else -> throw Exception("Operator not supported $p")
                                                        }
            }
            else -> when(p.p) {
                        "equal?" -> when {
                                        (vals[0] is CloV) or (vals[1] is CloV) -> BoolV(false)
                                        (vals[0] is PrimV) or (vals[1] is PrimV) -> BoolV(false)
                                        else -> BoolV(vals[0] == vals[1])
                                    }
                        else -> throw Exception("Not a valid 2 value operator $p")
            }
        }
        3 -> when(p.p) {
            "substring" -> if ((vals[0] is StrV) && (vals[1] is NumV) && (vals[2] is NumV)) {
                                val s = (vals[0] as StrV).s;
                                val start = ((vals[1] as NumV).n).toInt();
                                val end = ((vals[2] as NumV).n).toInt();
                                StrV(s.substring(start, end))
                            } else {
                                throw Exception("Improper types for substring $vals")
                            }
            else -> throw Exception("Too many values: $vals for operation: $p")
        }
        else -> throw Exception("Too many values: $vals for operation: $p")
    }
}


// make-bindings: binds each param to arg
fun makeBindings(p: ArrayList<String>, a: ArrayList<Value>, env: Env): Env {
    if (p.isEmpty()) {
        return env
    }
    env.b.add(Binding(p.first(), a.first()))
    return makeBindings(p.subList(1, p.size-1) as ArrayList<String>,
        a.subList(1, a.size-1) as ArrayList<Value>, env)
}

// env-lookup
fun envLookup(s: String, env: Env): Value {
    for (bind in env.b) {
        if (bind.name == s) {
            return bind.the_value
        }
    }
    throw Exception("env-lookup Unbound Identifier: $s")
}
//========================= PARSING =====================================

// parse: takes in an Sexp, returns an ExprC of the proper form
fun parse(p: Array<Any>): ExprC {
    return when(p.size) {
        1 -> when(val s = p[0]) {
            is Double -> NumC(s)
            is String -> if(s[0] == '"') {
                StrC(s)
            } else {
                if(s in reserved) throw Exception ("Invalid id $s") else IdC(s)
            }
            else -> throw Exception("Invalid singleton parse $s")
        }
        else -> when(p[0]) {
            "if" -> IfC(parse(prep(p[1])), parse(prep(p[2])), parse(prep(p[3])))
            "local" -> {val (s, exp) = localHelper(p[1] as Array<Any>);
                        AppC(LamC(s, parse(prep(p[3]))), exp.map{e -> parse(prep(e))});
            }
            "lam" -> {lamHelper(p[1] as Array<Any>);
                        LamC(p[1] as ArrayList<String>, parse(prep(p[2])))
            }
            else -> AppC(parse(prep(p[0])), ((p.drop(1)).map{ arg -> parse(prep(arg))}))
        }
    }
}

// prep: preps an input into an array regarless of type
fun prep(p: Any): Array<Any> {
    try {
        return (p as Array<Any>);
    } catch(e: Exception) {
        return arrayOf(p)
    }
}

// localHelper: takes in an array of local expressions, returns a pair of symbols used and expressions
fun localHelper(l: Array<Any>): Pair<ArrayList<String>, ArrayList<Any>> {
    val symbols = ArrayList<String>();
    val exp = ArrayList<Any>();
    for(expr in l) {
        when(val arg = (expr as Array<Any>)[0]) {
            in reserved -> throw Exception("Reserved argument $arg")
            else -> symbols.add(arg as String)
        }
        exp.add(expr[2])
    }
    if(symbols != symbols.distinct()) throw Exception("Duplicated arguments $symbols")
    return Pair(symbols, exp)
}

// lamHelper: takes in an array of symbols, returns a true if they are distinct and not reserved
fun lamHelper(l: Array<Any>): Boolean {
    if(l != l.distinct()) throw Exception("Duplicated arguments $l");
    for(s in l) if(s in reserved) throw Exception("Reserved argument $s");
    return true
}