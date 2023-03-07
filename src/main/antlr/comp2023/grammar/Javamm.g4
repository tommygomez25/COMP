grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

ID : [a-zA-Z_][a-zA-Z_0-9]* ;
INT : [0-9]+;
WS : [ \t\r\n]+ -> skip;
//HANDLE COMMENT
COMMENT : ('/*' .*? '*/'| '//' ~[\r\n]*) -> skip;


program :
    importDeclaration* classDeclaration EOF;

importDeclaration:
    'import' names+=ID ('.' names+=ID)* ';' #Import ;

classDeclaration :
    'class' className=ID ( 'extends' extendName=ID )? '{' ( varDeclaration )* ( methodDeclaration )* '}' #Class ;

varDeclaration :
    fieldType = type fieldName = ID ';' #Field;

methodDeclaration :
    accessType='public'? type methodName=ID '(' ( type variables+=ID ( ',' type variables+=ID )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}' #Method
    | accessType='public'? 'static' 'void' methodName='main' '(' 'String' '[' ']' name=ID ')' '{' ( varDeclaration )* ( statement )* '}' #Method;

type locals [boolean isArray = false]:
    typeName='int' ('[' ']'{$isArray = true;})?
    | typeName='boolean'
    | typeName='int'
    | typeName='String'
    | typeName=ID
    ;

statement :
        '{' ( statement )* '}' #Block
      | 'if' '(' expression ')' statement 'else' statement #IfElse
      | 'while' '(' expression ')' statement #While
      | expression ';' #Expr
      | name = ID '=' expression ';' #Assign
      | name = ID '[' expression ']' '=' expression ';' #ArrayAssign
      ;

expression
    : '!' expression #Not
    | expression op=('*'|'/') expression #BinaryOp
    | expression op=('+'|'-') expression #BinaryOp
    | expression op='<' expression #BinaryOp
    | expression op='&&' expression #BinaryOp
    | expression '[' expression ']' #ArrayAccess
    | expression '.' 'length' #ArrayLength
    | expression '.' ID '(' ( expression ( ',' expression )* )? ')' #MethodCall
    | 'new' 'int' '[' expression ']' #NewIntArray
    | 'new' ID '(' ')' #NewObject
    | '(' expression ')' #Parenthesis
    | INT #IntLiteral
    | 'true' #BoolLiteral
    | 'false' #BoolLiteral
    | name = ID #Id
    | 'this' #This
    ;