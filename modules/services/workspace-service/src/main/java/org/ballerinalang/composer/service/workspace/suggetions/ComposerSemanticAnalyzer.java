package org.ballerinalang.composer.service.workspace.suggetions;

import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.AnnotationAttributeDef;
import org.ballerinalang.model.AnnotationDef;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BTypeMapper;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.ConstDef;
import org.ballerinalang.model.GlobalVariableDef;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.NamespaceDeclaration;
import org.ballerinalang.model.Node;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.SimpleVariableDef;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.AndExpression;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.DivideExpr;
import org.ballerinalang.model.expressions.EqualExpression;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.GreaterEqualExpression;
import org.ballerinalang.model.expressions.GreaterThanExpression;
import org.ballerinalang.model.expressions.InstanceCreationExpr;
import org.ballerinalang.model.expressions.JSONArrayInitExpr;
import org.ballerinalang.model.expressions.JSONInitExpr;
import org.ballerinalang.model.expressions.KeyValueExpr;
import org.ballerinalang.model.expressions.LambdaExpression;
import org.ballerinalang.model.expressions.LessEqualExpression;
import org.ballerinalang.model.expressions.LessThanExpression;
import org.ballerinalang.model.expressions.MapInitExpr;
import org.ballerinalang.model.expressions.ModExpression;
import org.ballerinalang.model.expressions.MultExpression;
import org.ballerinalang.model.expressions.NotEqualExpression;
import org.ballerinalang.model.expressions.NullLiteral;
import org.ballerinalang.model.expressions.OrExpression;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.TypeConversionExpr;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.XMLCommentLiteral;
import org.ballerinalang.model.expressions.XMLElementLiteral;
import org.ballerinalang.model.expressions.XMLLiteral;
import org.ballerinalang.model.expressions.XMLPILiteral;
import org.ballerinalang.model.expressions.XMLQNameExpr;
import org.ballerinalang.model.expressions.XMLSequenceLiteral;
import org.ballerinalang.model.expressions.XMLTextLiteral;
import org.ballerinalang.model.expressions.variablerefs.FieldBasedVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.IndexBasedVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.SimpleVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.XMLAttributesRefExpr;
import org.ballerinalang.model.statements.AbortStmt;
import org.ballerinalang.model.statements.ActionInvocationStmt;
import org.ballerinalang.model.statements.AssignStmt;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.statements.BreakStmt;
import org.ballerinalang.model.statements.CommentStmt;
import org.ballerinalang.model.statements.ContinueStmt;
import org.ballerinalang.model.statements.ForkJoinStmt;
import org.ballerinalang.model.statements.FunctionInvocationStmt;
import org.ballerinalang.model.statements.IfElseStmt;
import org.ballerinalang.model.statements.NamespaceDeclarationStmt;
import org.ballerinalang.model.statements.ReplyStmt;
import org.ballerinalang.model.statements.ReturnStmt;
import org.ballerinalang.model.statements.ThrowStmt;
import org.ballerinalang.model.statements.TransactionStmt;
import org.ballerinalang.model.statements.TransformStmt;
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.util.semantics.SemanticAnalyzer;

/**
 * Semantic Analyzer which has some additional capabilities for Composer
 */
public class ComposerSemanticAnalyzer extends SemanticAnalyzer {

    private org.ballerinalang.composer.service.workspace.langserver.dto.Position position;
    private SymbolScope closestScope;

    public ComposerSemanticAnalyzer(BLangProgram programScope,
                                    org.ballerinalang.composer.service.workspace.langserver.dto.Position position) {
        super(programScope);
        this.position = position;
    }

    public void visit(BLangProgram bLangProgram) {
        super.visit(bLangProgram);
    }

    public void visit(BLangPackage bLangPackage) {
        super.visit(bLangPackage);
    }

    public void visit(BallerinaFile bFile) {
        super.visit(bFile);
    }

    public void visit(ImportPackage importPkg) {
        checkAndSetClosestScope(importPkg);
        super.visit(importPkg);
    }

    public void visit(ConstDef constant) {
        checkAndSetClosestScope(constant);
        super.visit(constant);
    }

    public void visit(GlobalVariableDef globalVar) {
        checkAndSetClosestScope(globalVar);
        super.visit(globalVar);
    }

    public void visit(Service service) {
        openScope(service);
        checkAndSetClosestScope(service);
        super.visit(service);
    }

    public void visit(BallerinaConnectorDef connectorDef) {
        openScope(connectorDef);
        checkAndSetClosestScope(connectorDef);
        super.visit(connectorDef);
    }

    public void visit(Resource resource) {
        openScope(resource);
        checkAndSetClosestScope(resource);
        super.visit(resource);
    }

    public void visit(BallerinaFunction function) {
        openScope(function);
        checkAndSetClosestScope(function);
        super.visit(function);
    }

    public void visit(BTypeMapper typeMapper) {
        checkAndSetClosestScope(typeMapper);
        super.visit(typeMapper);
    }

    public void visit(BallerinaAction action) {
        openScope(action);
        checkAndSetClosestScope(action);
        super.visit(action);
    }

    public void visit(Worker worker) {
        checkAndSetClosestScope(worker);
        super.visit(worker);
    }

    public void visit(AnnotationAttachment annotation) {
        checkAndSetClosestScope(annotation);
        super.visit(annotation);
    }

    public void visit(ParameterDef parameterDef) {
        checkAndSetClosestScope(parameterDef);
        super.visit(parameterDef);
    }

    public void visit(SimpleVariableDef variableDef) {
        checkAndSetClosestScope(variableDef);
        super.visit(variableDef);
    }

    public void visit(StructDef structDef) {
        checkAndSetClosestScope(structDef);
        super.visit(structDef);
    }

    public void visit(AnnotationAttributeDef annotationAttributeDef) {
        checkAndSetClosestScope(annotationAttributeDef);
        super.visit(annotationAttributeDef);
    }

    public void visit(AnnotationDef annotationDef) {
        checkAndSetClosestScope(annotationDef);
        super.visit(annotationDef);
    }

    // Statements

    public void visit(VariableDefStmt varDefStmt) {
        checkAndSetClosestScope(varDefStmt);
        super.visit(varDefStmt);
    }

    public void visit(AssignStmt assignStmt) {
        checkAndSetClosestScope(assignStmt);
        super.visit(assignStmt);
    }

    public void visit(BlockStmt blockStmt) {
        openScope(blockStmt);
        checkAndSetClosestScope(blockStmt);
        super.visit(blockStmt);
    }

    public void visit(CommentStmt commentStmt) {
        checkAndSetClosestScope(commentStmt);
        super.visit(commentStmt);
    }

    public void visit(IfElseStmt ifElseStmt) {
        checkAndSetClosestScope(ifElseStmt);
        super.visit(ifElseStmt);
    }

    public void visit(ReplyStmt replyStmt) {
        checkAndSetClosestScope(replyStmt);
        super.visit(replyStmt);
    }

    public void visit(ReturnStmt returnStmt) {
        checkAndSetClosestScope(returnStmt);
        super.visit(returnStmt);
    }

    public void visit(WhileStmt whileStmt) {
        checkAndSetClosestScope(whileStmt);
        super.visit(whileStmt);
    }

    public void visit(BreakStmt breakStmt) {
        checkAndSetClosestScope(breakStmt);
        super.visit(breakStmt);
    }

    public void visit(ContinueStmt continueStmt) {
        checkAndSetClosestScope(continueStmt);
        super.visit(continueStmt);
    }

    public void visit(TryCatchStmt tryCatchStmt) {
        checkAndSetClosestScope(tryCatchStmt);
        super.visit(tryCatchStmt);
    }

    public void visit(ThrowStmt throwStmt) {
        checkAndSetClosestScope(throwStmt);
        super.visit(throwStmt);
    }

    public void visit(FunctionInvocationStmt functionInvocationStmt) {
        checkAndSetClosestScope(functionInvocationStmt);
        super.visit(functionInvocationStmt);
    }

    public void visit(ActionInvocationStmt actionInvocationStmt) {
        checkAndSetClosestScope(actionInvocationStmt);
        super.visit(actionInvocationStmt);
    }

    public void visit(WorkerInvocationStmt workerInvocationStmt) {
        checkAndSetClosestScope(workerInvocationStmt);
        super.visit(workerInvocationStmt);
    }

    public void visit(WorkerReplyStmt workerReplyStmt) {
        checkAndSetClosestScope(workerReplyStmt);
        super.visit(workerReplyStmt);
    }

    public void visit(ForkJoinStmt forkJoinStmt) {
        openScope(forkJoinStmt);
        checkAndSetClosestScope(forkJoinStmt);
        super.visit(forkJoinStmt);
    }

    public void visit(TransformStmt transformStmt) {
        checkAndSetClosestScope(transformStmt);
        super.visit(transformStmt);
    }

    public void visit(TransactionStmt transactionStmt) {
        checkAndSetClosestScope(transactionStmt);
        super.visit(transactionStmt);
    }

    public void visit(AbortStmt abortStmt) {
        checkAndSetClosestScope(abortStmt);
        super.visit(abortStmt);
    }

    public void visit(NamespaceDeclarationStmt namespaceDeclarationStmt) {
        checkAndSetClosestScope(namespaceDeclarationStmt);
        super.visit(namespaceDeclarationStmt);
    }

    public void visit(NamespaceDeclaration namespaceDclr) {
        checkAndSetClosestScope(namespaceDclr);
        super.visit(namespaceDclr);
    }

    // Expressions

    public void visit(AddExpression addExpr) {
        checkAndSetClosestScope(addExpr);
        super.visit(addExpr);
    }

    public void visit(AndExpression andExpression) {
        checkAndSetClosestScope(andExpression);
        super.visit(andExpression);
    }

    public void visit(BasicLiteral basicLiteral) {
        checkAndSetClosestScope(basicLiteral);
        super.visit(basicLiteral);
    }

    public void visit(DivideExpr divideExpr) {
        checkAndSetClosestScope(divideExpr);
        super.visit(divideExpr);
    }

    public void visit(ModExpression modExpression) {
        checkAndSetClosestScope(modExpression);
        super.visit(modExpression);
    }

    public void visit(EqualExpression equalExpression) {
        checkAndSetClosestScope(equalExpression);
        super.visit(equalExpression);
    }

    public void visit(FunctionInvocationExpr functionInvocationExpr) {
        checkAndSetClosestScope(functionInvocationExpr);
        super.visit(functionInvocationExpr);
    }

    public void visit(ActionInvocationExpr actionInvocationExpr) {
        checkAndSetClosestScope(actionInvocationExpr);
        super.visit(actionInvocationExpr);
    }

    public void visit(GreaterEqualExpression greaterEqualExpression) {
        checkAndSetClosestScope(greaterEqualExpression);
        super.visit(greaterEqualExpression);
    }

    public void visit(GreaterThanExpression greaterThanExpression) {
        checkAndSetClosestScope(greaterThanExpression);
        super.visit(greaterThanExpression);
    }

    public void visit(LessEqualExpression lessEqualExpression) {
        checkAndSetClosestScope(lessEqualExpression);
        super.visit(lessEqualExpression);
    }

    public void visit(LessThanExpression lessThanExpression) {
        checkAndSetClosestScope(lessThanExpression);
        super.visit(lessThanExpression);
    }

    public void visit(MultExpression multExpression) {
        checkAndSetClosestScope(multExpression);
        super.visit(multExpression);
    }

    public void visit(InstanceCreationExpr instanceCreationExpr) {
        checkAndSetClosestScope(instanceCreationExpr);
        super.visit(instanceCreationExpr);
    }

    public void visit(NotEqualExpression notEqualExpression) {
        checkAndSetClosestScope(notEqualExpression);
        super.visit(notEqualExpression);
    }

    public void visit(OrExpression orExpression) {
        checkAndSetClosestScope(orExpression);
        super.visit(orExpression);
    }

    public void visit(SubtractExpression subtractExpression) {
        checkAndSetClosestScope(subtractExpression);
        super.visit(subtractExpression);
    }

    public void visit(UnaryExpression unaryExpression) {
        checkAndSetClosestScope(unaryExpression);
        super.visit(unaryExpression);
    }

    public void visit(TypeCastExpression typeCastExpression) {
        checkAndSetClosestScope(typeCastExpression);
        super.visit(typeCastExpression);
    }

    public void visit(TypeConversionExpr typeConversionExpression) {
        checkAndSetClosestScope(typeConversionExpression);
        super.visit(typeConversionExpression);
    }

    public void visit(ArrayInitExpr arrayInitExpr) {
        checkAndSetClosestScope(arrayInitExpr);
        super.visit(arrayInitExpr);
    }

    public void visit(RefTypeInitExpr refTypeInitExpr) {
        checkAndSetClosestScope(refTypeInitExpr);
        super.visit(refTypeInitExpr);
    }

    public void visit(ConnectorInitExpr connectorInitExpr) {
        checkAndSetClosestScope(connectorInitExpr);
        super.visit(connectorInitExpr);
    }

    public void visit(StructInitExpr structInitExpr) {
        checkAndSetClosestScope(structInitExpr);
        super.visit(structInitExpr);
    }

    public void visit(MapInitExpr mapInitExpr) {
        checkAndSetClosestScope(mapInitExpr);
        super.visit(mapInitExpr);
    }

    public void visit(JSONInitExpr jsonInitExpr) {
        checkAndSetClosestScope(jsonInitExpr);
        super.visit(jsonInitExpr);
    }

    public void visit(JSONArrayInitExpr jsonArrayInitExpr) {
        checkAndSetClosestScope(jsonArrayInitExpr);
        super.visit(jsonArrayInitExpr);
    }

    public void visit(KeyValueExpr keyValueExpr) {
        checkAndSetClosestScope(keyValueExpr);
        super.visit(keyValueExpr);
    }

    public void visit(SimpleVarRefExpr simpleVarRefExpr) {
        checkAndSetClosestScope(simpleVarRefExpr);
        super.visit(simpleVarRefExpr);
    }

    public void visit(FieldBasedVarRefExpr fieldBasedVarRefExpr) {
        checkAndSetClosestScope(fieldBasedVarRefExpr);
        super.visit(fieldBasedVarRefExpr);
    }

    public void visit(IndexBasedVarRefExpr indexBasedVarRefExpr) {
        checkAndSetClosestScope(indexBasedVarRefExpr);
        super.visit(indexBasedVarRefExpr);
    }

    public void visit(XMLAttributesRefExpr xmlAttributesRefExpr) {
        checkAndSetClosestScope(xmlAttributesRefExpr);
        super.visit(xmlAttributesRefExpr);
    }

    public void visit(XMLQNameExpr xmlQNameRefExpr) {
        checkAndSetClosestScope(xmlQNameRefExpr);
        super.visit(xmlQNameRefExpr);
    }

    public void visit(NullLiteral nullLiteral) {
        checkAndSetClosestScope(nullLiteral);
        super.visit(nullLiteral);
    }

    public void visit(XMLLiteral xmlLiteral) {
        checkAndSetClosestScope(xmlLiteral);
        super.visit(xmlLiteral);
    }

    public void visit(XMLElementLiteral xmlElement) {
        checkAndSetClosestScope(xmlElement);
        super.visit(xmlElement);
    }

    public void visit(XMLCommentLiteral xmlComment) {
        checkAndSetClosestScope(xmlComment);
        super.visit(xmlComment);
    }

    public void visit(XMLTextLiteral xmlText) {
        checkAndSetClosestScope(xmlText);
        super.visit(xmlText);
    }

    public void visit(XMLPILiteral xmlPI) {
        checkAndSetClosestScope(xmlPI);
        super.visit(xmlPI);
    }

    public void visit(XMLSequenceLiteral xmlSequence) {
        checkAndSetClosestScope(xmlSequence);
        super.visit(xmlSequence);
    }

    public void visit(LambdaExpression lambdaExpr) {
        checkAndSetClosestScope(lambdaExpr);
        super.visit(lambdaExpr);
    }

    private void checkAndSetClosestScope(Node node) {
        int startLineNumber = node.getNodeLocation().startLineNumber;
        int startColumn = node.getNodeLocation().startColumn;

        Position stop = new Position();
        getStopPosition(node, stop);

        if (position.getLine() > startLineNumber) {
            if (stop.lineNumber != -1 && stop.column != -1) {
                if (position.getLine() < stop.lineNumber) {
                    closestScope = getCurrentScope();
                } else if (position.getLine() == stop.lineNumber) {
                    if (position.getCharacter() <= stop.column) {
                        closestScope = getCurrentScope();
                    }
                }
            }
        } else if (position.getLine() == startLineNumber) {
            if (position.getCharacter() >= startColumn) {
                if (stop.lineNumber != -1 && stop.column != -1) {
                    if (position.getLine() < stop.lineNumber) {
                        closestScope = getCurrentScope();
                    } else if (position.getLine() == stop.lineNumber) {
                        if (position.getCharacter() <= stop.column) {
                            closestScope = getCurrentScope();
                        }
                    }
                }
            }
        }
    }

    private void getStopPosition(Node node, Position stopPosition) {
        NodeLocation location = node.getNodeLocation();
        if (location != null) {
            int stopLineNumber = location.stopLineNumber;
            int stopColumn = location.stopColumn;
            if (stopLineNumber == -1) {
                if (node instanceof SymbolScope) {
                    SymbolScope enclosingScope = ((SymbolScope) node).getEnclosingScope();
                    if (enclosingScope instanceof Node) {
                        Node parent = (Node) enclosingScope;
                        getStopPosition(parent, stopPosition);
                    }
                }
            } else {
                stopPosition.lineNumber = stopLineNumber;
                stopPosition.column = stopColumn;
            }
        }
        return;
    }

    public SymbolScope getClosestScope() {
        return closestScope;
    }

    private static class Position {
        int lineNumber;
        int column;
    }
}
