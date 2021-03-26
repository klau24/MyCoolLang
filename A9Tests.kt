import org.junit.Test
import kotlin.test.assertEquals
//class NumV(val n: Double) : Value()
//class BoolV(val b: Boolean) : Value()
//class StrV(val s: String) : Value()
//class CloV(val param: ArrayList<String>, val body: ExprC, val env: Env) : Value()
//class PrimV(val p: String) : Value()
class A9Tests {
    @Test
    fun mainTest() {
        val p1 = arrayOf<Any>("+", 9.0, 16.0)
        val p2 = arrayOf<Any>("z", "=", p1)
        val p3 = arrayOf<Any>("y", "=", 25.0)
        val p4 = arrayOf<Any>("+", "z", "y")
        val p5 = arrayOf<Any>(p2, p3)
        val p6 = arrayOf<Any>("local", p5, "in", p4)

        val a1 = arrayOf<Any>("if", arrayOf<Any>("<=", 3.0, 5.0), "\"Hi", 3.0)

        assertEquals(main(a1), "\"Hi")
        assertEquals(main(p6), "50.0")
    }

    @Test
    fun serializeTest() {
        val t1 = serialize(NumV(5.0))
        val t2 = serialize(BoolV(true))
        val t3 = serialize(StrV("hello"))
        val t4 = serialize(CloV(arrayListOf("a", "b"), NumC(5.3), Env(arrayListOf())))
        val t5 = serialize(PrimV("primitive"))
        assertEquals(t1, "5.0")
        assertEquals(t2, "true")
        assertEquals(t3, "hello")
        assertEquals(t4, "#<procedure>")
        assertEquals(t5, "#<primop>")
    }

    //
    @Test
    fun interpTest() {
        val topEnv = Env(arrayListOf<Binding>(
            Binding("+", PrimV("+")),
            Binding("-", PrimV("-")),
            Binding("*", PrimV("*")),
            Binding("/", PrimV("/")),
            Binding("<=", PrimV("<=")),
            Binding("equal?", PrimV("equal?")),
            Binding("true", BoolV(true)),
            Binding("false", BoolV(false)),
            Binding("error", PrimV("error")),
            Binding("substring", PrimV("substring")))
        )
        // '{local {[x = 5]} in {+ x 5}}
        val t1 = AppC(LamC(arrayListOf<String>("x"),
                           AppC(IdC("+"), arrayListOf<ExprC>(IdC("x"),NumC(5.0)))),
                      arrayListOf<ExprC>(NumC(5.0)))

        // '{lam {x y} {+ x y} 2 3}
        val t2 = AppC(LamC(arrayListOf<String>("x", "z"),
                           AppC(IdC("+"),
                           arrayListOf<ExprC>(IdC("x"), IdC("z")))),
                      arrayListOf<ExprC>(NumC(2.0), NumC(3.0)))
        //'{local
        //  [{a = {lam {x y} {+ x y}}}
        //   {b = {lam {z} {equal? z 4}}}]
        //  in
        //  {if {b {a 4 5}} "succ" "fai"}}
        val t3 = AppC(LamC(arrayListOf<String>("a", "b"), IfC(AppC(IdC("b"),
                           arrayListOf<ExprC>(AppC(IdC("a"), arrayListOf<ExprC>(NumC(4.0), NumC(5.0))))),
                           StrC("succ"), StrC("fail"))),
                      arrayListOf<ExprC>(LamC(arrayListOf<String>("x", "y"), AppC(IdC("+"),
                                         arrayListOf<ExprC>(IdC("x"), IdC("y")))),
                                         LamC(arrayListOf<String>("z"), AppC(IdC("equal?"), arrayListOf<ExprC>(IdC("z"), NumC(4.0))))))

        assertEquals(interp(t1, Env(ArrayList(topEnv.b))), NumV(10.0))
        assertEquals(interp(t2, Env(ArrayList(topEnv.b))), NumV(5.0))
        assertEquals(interp(t3, Env(ArrayList(topEnv.b))), StrV("fail"))

        assertEquals(interp(NumC(3.1), Env(ArrayList(topEnv.b))),  NumV(3.1))
        //3 + 4
        assertEquals(interp(AppC (IdC ("+"), arrayListOf(NumC (3.0), NumC (4.0))), Env(ArrayList(topEnv.b))), NumV(7.0))
        //3 - 4
        assertEquals(interp(AppC (IdC ("-"), arrayListOf(NumC (3.0), NumC (4.0))), Env(ArrayList(topEnv.b))), NumV(-1.0))
        //3 * 4
        assertEquals(interp(AppC (IdC ("*"), arrayListOf(NumC (3.0), NumC (4.0))), Env(ArrayList(topEnv.b))), NumV(12.0))
        // 3 / 4
        assertEquals(interp(AppC (IdC ("/"), arrayListOf(NumC (3.0), NumC (4.0))), Env(ArrayList(topEnv.b))), NumV(.75))
        // 3 <= 4
        assertEquals(interp(AppC (IdC ("<="), arrayListOf(NumC (3.0), NumC (4.0))), Env(ArrayList(topEnv.b))), BoolV(true))
        // 4 <= 3
        assertEquals(interp(AppC (IdC ("<="), arrayListOf(NumC (4.0), NumC (3.0))), Env(ArrayList(topEnv.b))), BoolV(false))
        // Ray equal? Ray
        assertEquals(interp(AppC (IdC ("equal?"), arrayListOf(StrC ("Ray"), StrC ("Ray"))), Env(ArrayList(topEnv.b))), BoolV(true))
        // Ray equal? ray
        assertEquals(interp(AppC (IdC ("equal?"), arrayListOf(StrC ("Ray"), StrC ("ray"))), Env(ArrayList(topEnv.b))), BoolV(false))
        // (substring Parth 3 4)
        assertEquals(interp(AppC (IdC ("substring"), arrayListOf(StrC("Parth"), NumC (3.0), NumC (4.0))), Env(ArrayList(topEnv.b))), StrV("t"))
    }




}