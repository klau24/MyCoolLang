# MyCoolLang
A basic language with parser and interpreter built in Kotlin

#Concrete syntax of the language
Expr =
- Num
- id
- String
- {if Expr Expr Expr}
- {local [{id = Expr} ...] in Expr}
- {lam {id ...} Expr}
- {Expr Expr ...}
        
... where an id is not local, in, if, or lam.

#Values
- Reals
- Booleans
- Strings
- Closures
- Primitives (+, -, /, *, <=, equal?)

