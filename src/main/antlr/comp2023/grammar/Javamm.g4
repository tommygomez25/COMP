grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

ID : [a-zA-Z$_][a-zA-Z$_0-9]*;
INT : [0]|[1-9][0-9]*;
WS : [ \t\r\n]+ -> skip;
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
    accessType='public'? type methodName=ID '(' ( type parameters+=ID ( ',' type parameters+=ID )* )? ')' '{' ( varDeclaration )* ( statement )* ret #Method
    | accessType='public'? 'static' 'void' methodName='main' '(' 'String' '[' ']' args=ID ')' '{' ( varDeclaration )* ( statement )* '}' #Method;

ret :
    'return' expression ';' '}' #ReturnFromMethod;

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
      | varName = ID '=' expression ';' #Assign
      | varName = ID '[' expression ']' '=' expression ';' #ArrayAssign
      ;

expression
    : '(' expression ')' #Parenthesis
    | expression '[' expression ']' #ArrayAccess
    | expression '.' 'length' #ArrayLength
    | '!' expression #Not
    | 'new' 'int' '[' expression ']' #NewIntArray
    | 'new' ID '(' ')' #NewObject
    | expression op=('*'|'/') expression #BinaryOp
    | expression op=('+'|'-') expression #BinaryOp
    | expression op='<' expression #BooleanOp
    | expression op='&&' expression #BooleanOp
    | expression '.' caller=ID '(' ( expression ( ',' expression )* )? ')' #MethodCall
    | var=INT #IntLiteral
    | var='true' #BoolLiteral
    | var='false' #BoolLiteral
    | name = ID #Id
    | 'this' #This
    ;

