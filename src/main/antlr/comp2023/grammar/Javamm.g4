grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

ID : [a-zA-Z_] [a-zA-Z_0-9]*;
INT : [0-9]+;
WS : [ \t\r\n]+ -> skip;
COMMENT : ('/*' .*? '*/'|'//' ~[\r\n]*) -> skip;

program :
    importDeclaration* classDeclaration EOF;

importDeclaration:
    'import' name+=ID ( '.' name+=ID )* ';'
    ;

classDeclaration :
    'class' className=ID ( 'extends' extendName=ID )? '{' ( varDeclaration )* ( methodDeclaration )* '}';

varDeclaration :
    type name=ID ';';

methodDeclaration :
    accessType='public'? type methodName=ID '(' ( type variables+=ID ( ',' type variables+=ID )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}'
    | accessType='public'? 'static' 'void' 'main' '(' 'String' '[' ']' name=ID ')' '{' ( varDeclaration )* ( statement )* '}';


type locals [boolean isArray=false]:
    name='int' ('[' ']' {$isArray=true;})?| name = 'boolean' | name='String' | name=ID ;

statement :
        '{' ( statement )* '}' #block
      | 'if' '(' expression ')' statement 'else' statement #ifElse
      | 'while' '(' expression ')' statement #while
      | expression ';' #expr
      | name=ID '=' expression ';' #assign
      | name=ID '[' expression ']' '=' expression ';' #arrayAssign
      ;

expression
    : '!' expression #not
    | expression op=('*'|'/') expression #binaryOp
    | expression op=('+'|'-') expression #binaryOp
    | expression op='<' expression #binaryOp
    | expression op='&&' expression #binaryOp
    | expression open='[' expression close=']' #arrayAccess
    | expression '.' 'length' #arrayLength
    | expression '.' ID '(' ( expression ( ',' expression )* )? ')' #methodCall
    | 'new' 'int' '[' expression ']' #newIntArray
    | 'new' object=ID '(' ')' #newObject
    | '(' expression ')' #parentheses
    | INT #intLiteral
    | 'true' #boolLiteral
    | 'false' #boolLiteral
    | name=ID #id
    | 'this' #this
    ;

