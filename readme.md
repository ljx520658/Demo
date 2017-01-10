## Understanding JSymbol
JSymbol is a library to extract symbol definitions from source code of different programming languages. For each supported language, there is a class implementing interface [SymbolExtractor](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/SymbolExtractor.java). The implementation parses source of target language and return a list of symbols representing static structure of the source. GitPlex then uses this list to do below things:

1. Construct [source outline](https://www.pmease.com/gitplex#source-outline). Root nodes of the outline are those symbols with a _null_ [parent](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/Symbol.java&mark=50.15-50.24). To determine children of a particular symbol, GitPlex will loop the list to find symbols with [parent](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/Symbol.java&mark=50.15-50.24) set to that symbol
1. Index names of [non-local](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/Symbol.java&mark=77.19-77.26) symbols for [symbol search](https://www.pmease.com/gitplex#code-search) and [cross reference](https://www.pmease.com/gitplex#jump-to-definition)

Let's check how it works in detail by looking at a simple Java source below:
```java
package com.example.logger;
import java.util.Date;

public class Logger {
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void log(String message) {
        Date date = new Date();
        System.out.println(date + ":" + name + ":" + message);
    }
}
```
The [JavaExtractor](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/java/JavaExtractor.java) will extract below list of symbols:

1. A [compilation unit](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/java/symbols/CompilationUnit.java) representing package _com.example.logger_
1. A [type definition](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/java/symbols/TypeDef.java) representing class _Logger_, with parent symbol set to compilation unit _com.example.logger_
1. A [field definition](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/java/symbols/FieldDef.java) representing field _name_, with parent symbol set to type definition _Logger_
1. A [method definition](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/java/symbols/MethodDef.java) representing method _getName()_, with parent symbol set to type definition _Logger_
1. A [method definition](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/java/symbols/MethodDef.java) representing method _setName(String)_, with parent symbol set to type definition _Logger_
1. A [method definition](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/java/symbols/MethodDef.java) representing method _log(String)_, with parent symbol set to type definition _Logger_
   
Note that symbols declared in method body (_date_ here) will not be extracted as we do not consider them in static structure of the source. Based on these extracted symbols, GitPlex can then do below things:

1. Displays source outline as:
> ![simple_example](http://git.pmease.com/gitplex/jsymbol/raw?revision=master&path=doc/simple-example.png)
1. Indexes _Logger_, _getName_, _setName_ and _log_ for symbol search and cross reference. Package symbol _com.example.logger_ will not be indexed as we intentionally set the symbol name as null. The field symbol _name_ will also not be indexed as it is a local symbol (private field) 

## How to contribute
1. Fork this repository (fork link can be found by clicking the ellipsis icon besides the repository name)
1. Clone the forked repository to your own machine, for instance:
```
git clone http://git.pmease.com/<your account name>/jsymbol d:\jsymbol
```
1. Make sure you have JDK 8 installed
1. Import _d:\jsymbol_ as Maven project into IntelliJ and use JDK 8 as project JDK. IntelliJ community edition will be fine. 
1. JSymbol replies on Maven to generate some source code. If there are strange errors, just re-import the project to have Maven re-generate required source code as below:
![reimport.png](http://git.pmease.com/gitplex/jsymbol/raw?revision=master&path=doc/reimport.png)
1. Make sure everything works fine by running contained unit tests
1. Contribute your symbol extractor for a new language following guides below
1. After finishing your work, push changes and send a pull request to original repository [gitplex/jsymbol](http://git.pmease.com/gitplex/jsymbol)

## Working with static languages

Extracting symbols from static language is quite straightforward: all symbol definitions are declared statically and can be parsed out. You may open an IDE for the target language to show outline of the source. What you need to do is to extract symbols so that they can be organized the same way as the IDE outline. For instance, Java extractor in JSymbol mimics Eclipse outline and even uses the same set of symbol icons. Below is an example of the outline composed from extracted Java symbols:
![java outline](http://git.pmease.com/gitplex/jsymbol/raw?revision=master&path=doc/java-extraction.png)

## Working with dynamic languages

Extracting symbols from dynamic language is a bit awkward as not all symbol definitions can be parsed out statically. Different frameworks of the language may have different favors of symbol definition. Let's check some examples below to see how [JavaScriptExtractor](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/javascript/JavaScriptExtractor.java) deals with JavaScript and its major frameworks:

---------------  

![case1](http://git.pmease.com/gitplex/jsymbol/raw?revision=master&path=doc/js-case1.png)
 
1. _globalVar_ will be extracted as [VariableSymbol](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/javascript/symbols/VariableSymbol.java) as global variables can be referenced by other files  
1. _globalFunc_ will be extracted as [FunctionSymbol](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/javascript/symbols/FunctionSymbol.java) as global functions can be referenced by other files
1. _localVar_ and _localFunc_ are not extracted as we do not need them in source outline

------------------

![case2](http://git.pmease.com/gitplex/jsymbol/raw?revision=master&path=doc/js-case2.png)
 
1. _globalVar_ and _globalFunc_ will be extracted as symbols
1. _globalVar.property1_ will be extracted as [ReferenceSymbol](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/javascript/symbols/ReferenceSymbol.java). The string _property1_ will be indexed while string _globalVar_ is kept in the symbol for context displaying purpose. We make parent of this symbol as _localFunc_ instead of _globalVar_ as it can be easily figured out. After all, our purpose is to get the static structure instead of runtime structure. As a result of this _localFunc_ is also included in the returned symbol list, but will not be indexed as it is local
1. _otherVar.property3_ will be extracted as reference symbol the same way as _globalVar.property1_
1. _localVar.property2_ will not be extracted as its root object _localVar_ is declared as local variable 

Note that here we parsed into the function body to find out definition _otherVar.property3_ and _globalVar.property1_ as symbols can be defined anywhere in a dynamic language.

-------------------------

![case3](http://git.pmease.com/gitplex/jsymbol/raw?revision=master&path=doc/js-case3.png)

1. _Polygon_ will be extracted as [ClassSymbol](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/javascript/symbols/ClassSymbol.java)
1. _this.height_ and _this.width_ will be extracted as [PropertySymbol](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/javascript/symbols/PropertySymbol.java). Note that only string _height_ and _width_ is indexed and _this_ is kept only for context displaying purpose. Their parent symbol _constructor(height, method)_ will also be included but will not be indexed as it is local
1. _get width()_ and _get height()_ will be extracted as [MethodSymbol](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/javascript/symbols/MethodSymbol.java). Note that only string _height_ and _width_ will be indexed and others are kept for context displaying purpose
    
---------------------------

![case4](http://git.pmease.com/gitplex/jsymbol/raw?revision=master&path=doc/js-case4.png)

1. By checking the _require_ function call, we know that variable _foo_ is a reference to node.js module './bar'. But it will not be referenced from other files according to node.js conventions. So we extract it as a local symbol to consist the outline only
1. The symbol _exports.pen_ will be extracted as an exported [ReferenceSymbol](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/javascript/symbols/ReferenceSymbol.java)

-----------------------------

![case5](http://git.pmease.com/gitplex/jsymbol/raw?revision=master&path=doc/js-case5.png)

Our extractor also parses anonymous function body used in a function call, so we extract _$.fn.greenify_ as a [ReferenceSymbol](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/javascript/symbols/ReferenceSymbol.java). Name of the symbol will be set to _greenify_ and _$.fn_ is kept in symbol for context displaying purpose. Note that the parent symbol here is a function symbol without name, as it is an anonymous function
    
------------------------------

![case6](http://git.pmease.com/gitplex/jsymbol/raw?revision=master&path=doc/js-case6.png)

Our extractor knows about JavaScript object literal declarations, and return all properties and methods as appropriate symbols

----------------------------

![case7](http://git.pmease.com/gitplex/jsymbol/raw?revision=master&path=doc/js-case7.png)

1. The extractor is aware of Vue.js framework, so it extracts symbols from parameter of _Vue.extend_ function call and put contained properties directly under the _Parent_ symbol
1. It knows about the way to define Vue global component, so it extract components _child_ and its properties as symbols
1. It also knows about the way to declare components via _new Vue()_ statement, so it extracts _my-component_ as a symbol

## Use a parser if possible

Writing symbol extractor for a new language will be easier if you can use a parser to get the AST first. If you plan to employ a parser to do the job, make sure that:

1. The parser implementation should be pure Java to be cross platform
1. The parser should be fast enough. The average time to parse a 500 line source should be less than 50 milli-seconds on a moderate CPU after JVM warming up

## Resort to ANTLR if no appropriate parser

In case there is not an appropriate parser for the target language, you will need to parse symbols from source. ANTLR can help a little bit here: it can parse the text into a stream of lexer tokens so that we can work at token level instead of character level. Follow below steps to have ANTLR generating lexer tokens for you:

1. Place [ANTLR grammar file](https://github.com/antlr/grammars-v4) of your language to the same package as your extractor class. Note that the grammar file should have an extension of _.g4_
1. Reimport the project to have ANTLR generating lexer and parser classes. Lexer and parser class is named as _<grammer name>Lexer_ and _<grammer name>Parser_ respectively. The parser is generally useless as it is too slow
1. Construct the [TokenStream](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/util/TokenStream.java) with help of generated lexer class as below:
```
TokenStream stream = new TokenStream(new YourLexer(new ANTLRInputStream(text)), TokenFilter.DEFAULT_CHANNEL);
```
1. Test the lexer against some typical sources to make sure it is fast enough. For a 500 lines source, it should complete its job within 10 milli-seconds after warming up
1. If lexer performance is fine, you can then work on the token stream to parse out symbols. It is still a tough task, as you need to check the grammar file carefully to analyze possible language structures. You may skip parsing some constructs if its content does not contribute to source outline or is not reachable from other files. For instance, we skip parsing Java method bodies in [JavaExtractor](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/main/java/com/gitplex/jsymbol/java/JavaExtractor.java) (We do not use an existing parser to parse Java source as the work to parse Java is not that complex with help of the token stream)

## Use JSyntax tokenizer as last resort

If ANTLR lexer is too slow, you can then use various tokenizers in [JSyntax](http://git.pmease.com/gitplex/jsyntax) project to lex the source. The token stream can be created this way:
```
TokenStream stream = new TokenStream(new CppTokenizer().tokenize(sourceLines), typeMapper);
```
Here _typeMapper_ tells how to get enum based token type given a [JSyntax token](http://git.pmease.com/gitplex/jsyntax/files?revision=master&path=src/main/java/com/gitplex/jsyntax/Token.java) instance. 

## Pick a language and start your work

Now you are prepared. Pick an unimplemented language below and start to contribute:

- [x] Java
- [x] JavaScript
- [ ] C
- [ ] C++
- [ ] CSharp 
- [ ] Python - possible parser: https://github.com/SonarSource/sonar-python/tree/master/python-squid
- [ ] Php - passible parser: https://github.com/SonarSource/sonar-php/tree/master/php-frontend
- [ ] Swift
- [ ] Go
- [ ] TypeScript
- [ ] Ruby

While writing your symbol extractor, we suggest to start simple: make it to extract symbols from structures you are familiar with, and then expand it to handle more structures. During this process, you can run [WebServer](http://git.pmease.com/gitplex/jsymbol/files?revision=master&path=src/test/java/com/gitplex/jsymbol/web/WebServer.java) to examine your result from time to time. 

Please place your symbol extractor and companion classes into package named by the target language. The extractor should be thread-safe, that is, the same *SymbolExtractor* instance can be used in different threads to extract from different sources concurrently. Also make sure to add appropriate JUnit test cases to verify correctness of your extractor.