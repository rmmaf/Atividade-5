package br.ufpe.cin.if688.minijava.visitor;


import br.ufpe.cin.if688.minijava.ast.And;
import br.ufpe.cin.if688.minijava.ast.ArrayAssign;
import br.ufpe.cin.if688.minijava.ast.ArrayLength;
import br.ufpe.cin.if688.minijava.ast.ArrayLookup;
import br.ufpe.cin.if688.minijava.ast.Assign;
import br.ufpe.cin.if688.minijava.ast.Block;
import br.ufpe.cin.if688.minijava.ast.BooleanType;
import br.ufpe.cin.if688.minijava.ast.Call;
import br.ufpe.cin.if688.minijava.ast.ClassDeclExtends;
import br.ufpe.cin.if688.minijava.ast.ClassDeclSimple;
import br.ufpe.cin.if688.minijava.ast.Exp;
import br.ufpe.cin.if688.minijava.ast.False;
import br.ufpe.cin.if688.minijava.ast.Formal;
import br.ufpe.cin.if688.minijava.ast.Identifier;
import br.ufpe.cin.if688.minijava.ast.IdentifierExp;
import br.ufpe.cin.if688.minijava.ast.IdentifierType;
import br.ufpe.cin.if688.minijava.ast.If;
import br.ufpe.cin.if688.minijava.ast.IntArrayType;
import br.ufpe.cin.if688.minijava.ast.IntegerLiteral;
import br.ufpe.cin.if688.minijava.ast.IntegerType;
import br.ufpe.cin.if688.minijava.ast.LessThan;
import br.ufpe.cin.if688.minijava.ast.MainClass;
import br.ufpe.cin.if688.minijava.ast.MethodDecl;
import br.ufpe.cin.if688.minijava.ast.Minus;
import br.ufpe.cin.if688.minijava.ast.NewArray;
import br.ufpe.cin.if688.minijava.ast.NewObject;
import br.ufpe.cin.if688.minijava.ast.Not;
import br.ufpe.cin.if688.minijava.ast.Plus;
import br.ufpe.cin.if688.minijava.ast.Print;
import br.ufpe.cin.if688.minijava.ast.Program;
import br.ufpe.cin.if688.minijava.ast.This;
import br.ufpe.cin.if688.minijava.ast.Times;
import br.ufpe.cin.if688.minijava.ast.True;
import br.ufpe.cin.if688.minijava.ast.Type;
import br.ufpe.cin.if688.minijava.ast.VarDecl;
import br.ufpe.cin.if688.minijava.ast.While;
import br.ufpe.cin.if688.minijava.symboltable.Class;
import br.ufpe.cin.if688.minijava.symboltable.Method;
import br.ufpe.cin.if688.minijava.symboltable.SymbolTable;
import br.ufpe.cin.if688.minijava.symboltable.Variable;

public class TypeCheckVisitor implements IVisitor<Type> {

	private SymbolTable symbolTable;
	private Class currClass;
	private Method currMethod;
	private String main;
	public TypeCheckVisitor(SymbolTable st) {
		symbolTable = st;
	}

	// MainClass m;
	// ClassDeclList cl;
	public Type visit(Program n) {
		n.m.accept(this);
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.elementAt(i).accept(this);
		}
		return null;
	}

	// Identifier i1,i2;
	// Statement s;
	public Type visit(MainClass n) {
		main = n.i1.s;
		
		this.currClass = this.symbolTable.getClass(n.i1.s);
		this.currMethod = this.symbolTable.getMethod("main", currClass.getId());

		n.i1.accept(this);
		n.i2.accept(this);
		n.s.accept(this);

		this.currClass = null;
		this.currMethod = null;

		return null;
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclSimple n) {
		this.currClass = this.symbolTable.getClass(n.i.s);

		n.i.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}

		this.currClass = null;
		return null;
	}
	
	private void checagemClasseMae (String atual, String mae) {
		if(mae.equals(this.main)) {
			System.err.println("ERRO: nao eh possivel extender a main");
			System.exit(0);
		} else if (this.symbolTable.getClass(mae) == null) {
			System.err.println("ERRO: classe mae nao definida");
			System.exit(0);
		} else {
			while(mae != null) {//ir "a fundo" nas classes maes
				if(atual.equals(mae)) {
					System.err.println("ERRO: inconsistencia (heranca ciclica)");
					System.exit(0);
				} try {
					mae = symbolTable.getClass(mae).parent();
				} catch (NullPointerException e) {
					System.exit(0);
				}
			}
		}
	}
	
	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclExtends n) {
		this.currClass = this.symbolTable.getClass(n.i.s);
		n.i.accept(this);
		n.j.accept(this);
		checagemClasseMae(n.i.s, n.j.s);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}

		this.currClass = null;
		return null;
	}

	// Type t;
	// Identifier i;
	public Type visit(VarDecl n) {
		n.i.accept(this);
		return n.t.accept(this);//retorna o tipo da variavel para futura checagem
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public Type visit(MethodDecl n) {

		this.currMethod = this.symbolTable.getMethod(n.i.s,  this.currClass.getId());

		Type tipo = n.t.accept(this);
		n.i.accept(this);
		for (int i = 0; i < n.fl.size(); i++) {
			n.fl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		Type retorno = n.e.accept(this);
		boolean check = this.symbolTable.compareTypes(tipo, retorno);
		if(!check) {
			System.err.println("Erro: Inconsistência de tipos");
			System.exit(0);
		}
		this.currMethod = null;
		return tipo;
	}

	// Type t;
	// Identifier i;
	public Type visit(Formal n) {
		return n.t;
	}

	public Type visit(IntArrayType n) {
		return n;
	}

	public Type visit(BooleanType n) {
		return n;
	}

	public Type visit(IntegerType n) {
		return n;
	}

	// String s;
	public Type visit(IdentifierType n) {
		return n;
	}

	// StatementList sl;
	public Type visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		return null;
	}

	// Exp e;
	// Statement s1,s2;
	public Type visit(If n) {

		Type tipoFinal = n.e.accept(this);
		boolean check = this.symbolTable.compareTypes(tipoFinal, new BooleanType());
		if(!check) {
			System.err.println("Não é possível converter para Boolean" );
			System.exit(0);
		}
		n.s1.accept(this);
		n.s2.accept(this);
		return null;
	}

	// Exp e;
	// Statement s;
	public Type visit(While n) {
		Type tipo = n.e.accept(this);
		boolean check = this.symbolTable.compareTypes(tipo, new BooleanType());
		if(!check) {
			System.err.println("Não é possível converter para Boolean" );
			System.exit(0);
		}
		n.s.accept(this);
		return null;
	}

	// Exp e;
	public Type visit(Print n) {
		n.e.accept(this);
		return null;
	}

	// Identifier i;
	// Exp e;
	public Type visit(Assign n) {
		Type tipoDeclarado= this.symbolTable.getVarType(this.currMethod, this.currClass, n.i.s);
		Type tipoUsado = n.e.accept(this);
		boolean check = this.symbolTable.compareTypes(tipoUsado, tipoDeclarado);
		if(!check) {
			System.err.println("Erro de correspondência de tipos");
			System.exit(0);
		}
		n.i.accept(this);
		return null;
	}

	// Identifier i;
	// Exp e1,e2;
	public Type visit(ArrayAssign n) {
		
		Type variavelDeclardaTipo= this.symbolTable.getVarType(this.currMethod, this.currClass, n.i.s);
		Type variavelUsedaTipo = n.i.accept(this);
		boolean check = this.symbolTable.compareTypes(variavelDeclardaTipo, variavelUsedaTipo);
		if(!check) {
			System.err.println("Erro: estava esperando IntegerType");
			System.exit(0);
		}

		n.e1.accept(this);
		
		Type index = n.e2.accept(this);
		if(!(index instanceof IntegerType )) {
			System.err.println("Erro: estava esperando IntegerType");
			System.exit(0);
		}
		return null;
	}

	// Exp e1,e2;
	public Type visit(And n) {
		Type esquerda = n.e1.accept(this);
		Type direita = n.e2.accept(this);
		Type bool = new BooleanType();
		boolean aux1, aux2, check;
		aux1 = this.symbolTable.compareTypes(esquerda, bool);
		aux2 = this.symbolTable.compareTypes(direita, bool);
		check = aux1 && aux2;
		if(!check) {
			System.err.println("Erro: operador AND requer argumentos booleanos ");
			System.exit(0);
		}

		return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(LessThan n) {
		Type esquerda = n.e1.accept(this);
		Type direita = n.e2.accept(this);
		Type integer = new IntegerType();
		boolean aux1, aux2, check;
		aux1 = this.symbolTable.compareTypes(esquerda, integer);
		aux2 = this.symbolTable.compareTypes(direita, integer);
		check = aux1 && aux2;
		if(!check) {
			System.err.println("Erro: operador 'Menor que' requer argumentos do tipo Inteiro");
			System.exit(0);
		}

		return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(Plus n) {
		Type esquerda = n.e1.accept(this);
		Type direita = n.e2.accept(this);
		Type integer = new IntegerType();
		boolean aux1, aux2, check;
		aux1 = this.symbolTable.compareTypes(esquerda, integer);
		aux2 = this.symbolTable.compareTypes(direita, integer);
		check = aux1 && aux2;
		if(!check) {
			System.err.println("Erro: operador 'Soma' requer argumentos do tipo Inteiro");
			System.exit(0);
		}

		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Minus n) {
		Type esquerda = n.e1.accept(this);
		Type direita = n.e2.accept(this);
		Type integer = new IntegerType();
		boolean aux1, aux2, check;
		aux1 = this.symbolTable.compareTypes(esquerda, integer);
		aux2 = this.symbolTable.compareTypes(direita, integer);
		check = aux1 && aux2;
		if(!check) {
			System.err.println("Erro: operador 'Subtração' requer argumentos do tipo Inteiro");
			System.exit(0);
		}

		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Times n) {
		Type esquerda = n.e1.accept(this);
		Type direita = n.e2.accept(this);
		Type integer = new IntegerType();
		boolean aux1, aux2, check;
		aux1 = this.symbolTable.compareTypes(esquerda, integer);
		aux2 = this.symbolTable.compareTypes(direita, integer);
		check = aux1 && aux2;
		if(!check) {
			System.err.println("Erro: operador 'Multiplicação' requer argumentos do tipo Inteiro");
			System.exit(0);
		}

		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(ArrayLookup n) {
		Type tipo1 = n.e1.accept(this);
		Type tipo2 = n.e2.accept(this);

		boolean check = this.symbolTable.compareTypes(tipo1, new IntArrayType());
		if(!check) {
			System.err.println("Erro de tipo: esperava IntArrayType");
			System.exit(0);
		}
		check = this.symbolTable.compareTypes(tipo2, new IntegerType());
		if(!check) {
			System.err.println("Erro: aguardava um Inteiro");
			System.exit(0);
		}
		return new IntegerType();
	}

	// Exp e;
	public Type visit(ArrayLength n) {
		Type tipo = n.e.accept(this);
		boolean check = this.symbolTable.compareTypes(tipo, new IntArrayType());
		if(!check) {
			System.err.println("Erro: aguardava um IntArrayType");
			System.exit(0);
		}
		return new IntegerType();
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public Type visit(Call n) {
		Type tipoRetorno = null;

		Type checkTipo = n.e.accept(this);
		
		if(n.e instanceof This) {
			tipoRetorno = this.currClass.getMethod(n.i.s).type();
		} else if(checkTipo instanceof IdentifierType) {
			Class classeChamada = this.symbolTable.getClass(((IdentifierType) checkTipo).s);
			Method metodoChamado = this.symbolTable.getMethod(n.i.toString(), classeChamada.getId());
			Class classeAtual = this.currClass;
			this.currClass = classeChamada;


			int cont;
			for ( cont = 0; cont < n.el.size(); cont++) {
				Type tiposParametros = n.el.elementAt(cont).accept(this);

				Variable parametrosDeclarados = metodoChamado.getParamAt(cont);
				if(parametrosDeclarados == null) {
					System.err.println("Erro: parâmetro nulo ou ausente na chamda do método");
					System.exit(0);
				}
				Type tiposParametrosDeclarados = metodoChamado.getParamAt(cont).type();
				boolean check = this.symbolTable.compareTypes(tiposParametros, tiposParametrosDeclarados);
				if(!check) {
					System.err.println("Erro nos tipos");
					System.exit(0);
				}
			}
			if(metodoChamado.getParamAt(cont) != null) {
				System.err.println("Erro: parâmetro nulo ou ausente na chamda do método");
				System.exit(0);
			}
			Type tipoID = n.i.accept(this);
			this.currClass = classeAtual;
			return tipoID;
		}

		return tipoRetorno;
	}

	// int i;
	public Type visit(IntegerLiteral n) {
		return new IntegerType();
	}

	public Type visit(True n) {
		return new BooleanType();
	}

	public Type visit(False n) {
		return new BooleanType();
	}

	// String s;
	public Type visit(IdentifierExp n) {
		Type t = this.symbolTable.getVarType(this.currMethod, this.currClass, n.s);
		return t;
	}

	public Type visit(This n) {
		return this.currClass.type();
	}


	// Exp e;
	public Type visit(NewArray n) {
		Type index = n.e.accept(this);
		boolean check = this.symbolTable.compareTypes(index, new IntegerType());
		if(!check) {
			System.err.println("Erro: valor inteiro esperado");
			System.exit(0);
		}

		return new IntArrayType();
	}

	public Type visit(NewObject n) {
		return n.i.accept(this);
	}

	// Exp e;
	public Type visit(Not n) {
		Type expType = n.e.accept(this);
		if(!this.symbolTable.compareTypes(expType, new BooleanType())) {
			System.err.println("Erro: valor booleano esperado no 'NOT");
			System.exit(0);
		}
		return new BooleanType();
	}


	// String s;
	public Type visit(Identifier n) {

		if(this.currClass.containsVar(n.s)) {
			return symbolTable.getVarType(this.currMethod, this.currClass, n.s);
		}
		else if(this.currClass.containsMethod(n.s)) {
			return this.symbolTable.getMethodType(n.s, this.currClass.getId());
		}
		else if(this.currMethod != null && this.currMethod.containsVar(n.s)) {
			return this.currMethod.getVar(n.s).type();
		}
		else if(this.currMethod != null && this.currMethod.containsParam(n.s)) {
			return this.currMethod.getParam(n.s).type();
		}
		return null;
	}
}