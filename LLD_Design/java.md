# Java
- [Pointers in Java](#pointers-in-java)
- [Generics](#generics)
- [Difference from CPP - Quick Refernce](#java-for-c-developers-quick-reference)

## Pointers in Java
- Primitive are passed by value (int, double)
- Object (Anything created with class) is passed by reference
- Can't do pointer arithmetic
- Can't pass any Object by value. See [Pass By Value](#handling-pass-by-value-for-objects-in-java) if you have to.
- GC can't take out open resources, use try-with-resource and autoCloseable to close.
- No delete keyword, GC(garbage collection) takes care of every non-reachable block of occupied memory.
- No ```->``` operator, since everything is reference, ```.``` itself works like ``->``.
- `null` instead of `NULL` or `nullptr`
- [Generics](#generics) instead of templatisation.
## Generics
- in CPP we were using `template<typenam T>` to templeatize, but Java has generics.
- 
## Java for C++ Developers: Quick Reference

### 1. Core Runtime Differences
| Feature | C++                                                                | Java                                                                                                                                   |
| :--- |:-------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------|
| **Compilation** | Direct to Machine Code                                             | Bytecode (.class) -> JVM                                                                                                               |
| **Memory** | Manual (new/delete/RAII - Resource Acquisition in Initialisations) | Automatic (Garbage Collection)<br/> - for resources(like db connection) you will have to close manually, try-with-resource to be used. |
| **Pointers** | Raw Pointers, Smart Pointers                                       | References Only (No Arithmetic)                                                                                                        |
| **Multiple Inheritance** | Supported (Classes)                                                | Interfaces Only (Multiple), Classes (Single)                                                                                           |

### 2. Memory Behavior
- **Stack:** Stores primitives and local reference variables.
- **Heap:** All objects (instantiated via `new`) live here.
- **Null Safety:** All object references can be `null`. Accessing them throws `NullPointerException` (NPE).

### 3. Object Model
- **All functions are methods:** No global functions. Everything must exist inside a class.
- **Virtual by default:** All non-static methods are polymorphic (like `virtual` in C++). To prevent overriding, use the `final` keyword.
- **The Root:** Every class implicitly inherits from `java.lang.Object`.

### 4. Resource Management (The RAII Alternative)
Instead of destructors, use **Try-With-Resources**:
```java
try (BufferedReader br = new BufferedReader(new FileReader(path))) {
    return br.readLine();
} // br is automatically closed here
```
### Handling "Pass by Value" for Objects in Java

#### 1. The Reality Check
- **Java is strictly Pass-by-Value.** - For primitives: The actual bit pattern is copied.
- For objects: The **reference (pointer)** is copied.
- Result: You are always operating on the original object via a copied pointer.

#### 2. Simulating C++ Pass-by-Value
Since there is no syntax like `void func(MyClass obj)`, use these patterns:

| Strategy | Implementation | C++ Equivalent |
| :--- | :--- | :--- |
| **Copy Constructor** | `public MyClass(MyClass other) { ... }` | `MyClass(const MyClass &other)` |
| **Clone Method** | Implement `Cloneable` interface (Avoid this, it is buggy) | `operator=` / Deep Copy |
| **Defensive Copy** | `void logic(Obj o) { Obj local = new Obj(o); }` | Explicit local copy |
| **Immutability** | Use `record` or `final` fields | `const` object |

#### 3. Best Practice: Records (Java 16+)
If you just need a data carrier, use a `Record`. They are immutable by design.
```java
public record User(String name, int age) {} 
// Any "change" requires creating a brand new instance.
```
