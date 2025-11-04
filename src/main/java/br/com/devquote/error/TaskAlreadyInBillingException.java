package br.com.devquote.error;
public class TaskAlreadyInBillingException extends BusinessException {

    public TaskAlreadyInBillingException(Long taskId, String billingPeriod) {
        super(String.format("A tarefa ID %d já está incluída no faturamento do período %s. " +
              "Uma tarefa não pode estar em múltiplos períodos de faturamento simultaneamente.", taskId, billingPeriod), 
              "TASK_ALREADY_IN_BILLING");
    }
}