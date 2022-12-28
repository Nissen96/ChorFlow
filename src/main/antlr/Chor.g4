grammar Chor;

program: procedureList choreography;

procedureList: (procedure (',' procedure)*)?;
procedure: ID '(' processList ')' '=' choreography;

choreography: instruction ';' choreography
            | ZERO
            ;

instruction: interaction
           | assignment
           | selection
           | conditional
           | procedureCall
           | '(' instruction ')'
           ;

interaction: process '.' expression '->' process '.' ID;
assignment: process '.' ID ':=' expression;
selection: process '->' process '[' ID ']';
conditional: 'if' process '.' expression 'then' choreography 'else' choreography;
procedureCall: ID '(' processList ')';
processList: process ',' process (',' process)*;  // At least two processes
process: ID;

expression: op=('-' | '!') expression
          | expression op=('**' | '*' | '/' | '%' | '+' | '-') expression
          | expression op=('==' | '!=' | '<' | '>' | '<=' | '>=') expression
          | expression op=('&&' | '||') expression
          | '(' expression ')'
          | '(' expression (',' expression)+ ')'
          | ID '(' (expression (',' expression)*)? ')'
          | ID
          | (ZERO | INT)
          | BOOL
          | STR
          ;


WHITESPACE: [ \t\r\n] -> skip;
COMMENT: '/*' .*? '*/' -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;
ID  : [a-zA-Z][A-Za-z_0-9]*;

// 0 acts as the terminated choreography, need to split up INT rule to avoid competing lexemes
ZERO: '0';
INT : [0-9][0-9]+ | [1-9][0-9]*;
BOOL: 'true' | 'false';
STR : '"' ~["\\]* '"' { setText(getText().substring(1, getText().length() - 1)); };
