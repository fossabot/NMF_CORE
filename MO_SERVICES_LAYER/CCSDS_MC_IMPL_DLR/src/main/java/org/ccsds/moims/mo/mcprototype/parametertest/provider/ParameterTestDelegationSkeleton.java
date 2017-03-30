package org.ccsds.moims.mo.mcprototype.parametertest.provider;

/**
 * Provider Delegation skeleton for ParameterTestDelegationSkeleton service.
 */
public class ParameterTestDelegationSkeleton implements org.ccsds.moims.mo.mal.provider.MALInteractionHandler, org.ccsds.moims.mo.mcprototype.parametertest.provider.ParameterTestSkeleton
{
  private org.ccsds.moims.mo.mal.provider.MALProviderSet providerSet = new org.ccsds.moims.mo.mal.provider.MALProviderSet(org.ccsds.moims.mo.mcprototype.parametertest.ParameterTestHelper.PARAMETERTEST_SERVICE);
  private org.ccsds.moims.mo.mcprototype.parametertest.provider.ParameterTestHandler delegate;
  /**
   * Creates a delegation skeleton using the supplied delegate.
   * @param delegate delegate The interaction handler used for delegation.
   */
  public ParameterTestDelegationSkeleton(org.ccsds.moims.mo.mcprototype.parametertest.provider.ParameterTestHandler delegate)
  {
    this.delegate = delegate;
    delegate.setSkeleton(this);
  }

  /**
   * Adds the supplied MAL provider to the internal list of providers used for PubSub.
   * @param provider provider The provider to add.
   * @throws org.ccsds.moims.mo.mal.MALException If an error is detected.
   */
  public void malInitialize(org.ccsds.moims.mo.mal.provider.MALProvider provider) throws org.ccsds.moims.mo.mal.MALException
  {
    providerSet.addProvider(provider);
  }

  /**
   * Removes the supplied MAL provider from the internal list of providers used for PubSub.
   * @param provider provider The provider to add.
   * @throws org.ccsds.moims.mo.mal.MALException If an error is detected.
   */
  public void malFinalize(org.ccsds.moims.mo.mal.provider.MALProvider provider) throws org.ccsds.moims.mo.mal.MALException
  {
    providerSet.removeProvider(provider);
  }

  /**
   * Called by the provider MAL layer on reception of a message to handle the interaction.
   * @param interaction interaction the interaction object.
   * @param body body the message body.
   * @throws org.ccsds.moims.mo.mal.MALException if there is a internal error.
   * @throws org.ccsds.moims.mo.mal.MALInteractionException if there is a operation interaction error.
   */
  public void handleSend(org.ccsds.moims.mo.mal.provider.MALInteraction interaction, org.ccsds.moims.mo.mal.transport.MALMessageBody body) throws org.ccsds.moims.mo.mal.MALInteractionException, org.ccsds.moims.mo.mal.MALException
  {
    switch (interaction.getOperation().getNumber().getValue())
    {
      default:
        throw new org.ccsds.moims.mo.mal.MALInteractionException(new org.ccsds.moims.mo.mal.MALStandardError(org.ccsds.moims.mo.mal.MALHelper.UNSUPPORTED_OPERATION_ERROR_NUMBER, new org.ccsds.moims.mo.mal.structures.Union("Unknown operation")));
    }
  }

  /**
   * Called by the provider MAL layer on reception of a message to handle the interaction.
   * @param interaction interaction the interaction object.
   * @param body body the message body.
   * @throws org.ccsds.moims.mo.mal.MALException if there is a internal error.
   * @throws org.ccsds.moims.mo.mal.MALInteractionException if there is a operation interaction error.
   */
  public void handleSubmit(org.ccsds.moims.mo.mal.provider.MALSubmit interaction, org.ccsds.moims.mo.mal.transport.MALMessageBody body) throws org.ccsds.moims.mo.mal.MALInteractionException, org.ccsds.moims.mo.mal.MALException
  {
    switch (interaction.getOperation().getNumber().getValue())
    {
      case org.ccsds.moims.mo.mcprototype.parametertest.ParameterTestHelper._PUSHPARAMETERVALUE_OP_NUMBER:
        delegate.pushParameterValue((org.ccsds.moims.mo.mal.structures.Identifier) body.getBodyElement(0, new org.ccsds.moims.mo.mal.structures.Identifier()), (org.ccsds.moims.mo.mc.parameter.structures.ParameterRawValue) body.getBodyElement(1, new org.ccsds.moims.mo.mc.parameter.structures.ParameterRawValue()), interaction);
        interaction.sendAcknowledgement();
        break;
      case org.ccsds.moims.mo.mcprototype.parametertest.ParameterTestHelper._SETVALIDITYSTATEOPTIONS_OP_NUMBER:
        delegate.setValidityStateOptions((body.getBodyElement(0, new org.ccsds.moims.mo.mal.structures.Union(Boolean.FALSE)) == null) ? null : ((org.ccsds.moims.mo.mal.structures.Union) body.getBodyElement(0, new org.ccsds.moims.mo.mal.structures.Union(Boolean.FALSE))).getBooleanValue(), (body.getBodyElement(1, new org.ccsds.moims.mo.mal.structures.Union(Boolean.FALSE)) == null) ? null : ((org.ccsds.moims.mo.mal.structures.Union) body.getBodyElement(1, new org.ccsds.moims.mo.mal.structures.Union(Boolean.FALSE))).getBooleanValue(), interaction);
        interaction.sendAcknowledgement();
        break;
      case org.ccsds.moims.mo.mcprototype.parametertest.ParameterTestHelper._SETREADONLYPARAMETER_OP_NUMBER:
        delegate.setReadOnlyParameter((org.ccsds.moims.mo.mal.structures.Identifier) body.getBodyElement(0, new org.ccsds.moims.mo.mal.structures.Identifier()), (body.getBodyElement(1, new org.ccsds.moims.mo.mal.structures.Union(Boolean.FALSE)) == null) ? null : ((org.ccsds.moims.mo.mal.structures.Union) body.getBodyElement(1, new org.ccsds.moims.mo.mal.structures.Union(Boolean.FALSE))).getBooleanValue(), interaction);
        interaction.sendAcknowledgement();
        break;
      case org.ccsds.moims.mo.mcprototype.parametertest.ParameterTestHelper._SETPROVIDEDINTERVALS_OP_NUMBER:
        delegate.setProvidedIntervals((org.ccsds.moims.mo.mal.structures.DurationList) body.getBodyElement(0, new org.ccsds.moims.mo.mal.structures.DurationList()), interaction);
        interaction.sendAcknowledgement();
        break;
      case org.ccsds.moims.mo.mcprototype.parametertest.ParameterTestHelper._DELETEPARAMETERVALUES_OP_NUMBER:
        delegate.deleteParameterValues(interaction);
        interaction.sendAcknowledgement();
        break;
      default:
        interaction.sendError(new org.ccsds.moims.mo.mal.MALStandardError(org.ccsds.moims.mo.mal.MALHelper.UNSUPPORTED_OPERATION_ERROR_NUMBER, new org.ccsds.moims.mo.mal.structures.Union("Unknown operation")));
    }
  }

  /**
   * Called by the provider MAL layer on reception of a message to handle the interaction.
   * @param interaction interaction the interaction object.
   * @param body body the message body.
   * @throws org.ccsds.moims.mo.mal.MALException if there is a internal error.
   * @throws org.ccsds.moims.mo.mal.MALInteractionException if there is a operation interaction error.
   */
  public void handleRequest(org.ccsds.moims.mo.mal.provider.MALRequest interaction, org.ccsds.moims.mo.mal.transport.MALMessageBody body) throws org.ccsds.moims.mo.mal.MALInteractionException, org.ccsds.moims.mo.mal.MALException
  {
    switch (interaction.getOperation().getNumber().getValue())
    {
      default:
        interaction.sendError(new org.ccsds.moims.mo.mal.MALStandardError(org.ccsds.moims.mo.mal.MALHelper.UNSUPPORTED_OPERATION_ERROR_NUMBER, new org.ccsds.moims.mo.mal.structures.Union("Unknown operation")));
    }
  }

  /**
   * Called by the provider MAL layer on reception of a message to handle the interaction.
   * @param interaction interaction the interaction object.
   * @param body body the message body.
   * @throws org.ccsds.moims.mo.mal.MALException if there is a internal error.
   * @throws org.ccsds.moims.mo.mal.MALInteractionException if there is a operation interaction error.
   */
  public void handleInvoke(org.ccsds.moims.mo.mal.provider.MALInvoke interaction, org.ccsds.moims.mo.mal.transport.MALMessageBody body) throws org.ccsds.moims.mo.mal.MALInteractionException, org.ccsds.moims.mo.mal.MALException
  {
    switch (interaction.getOperation().getNumber().getValue())
    {
      default:
        interaction.sendError(new org.ccsds.moims.mo.mal.MALStandardError(org.ccsds.moims.mo.mal.MALHelper.UNSUPPORTED_OPERATION_ERROR_NUMBER, new org.ccsds.moims.mo.mal.structures.Union("Unknown operation")));
    }
  }

  /**
   * Called by the provider MAL layer on reception of a message to handle the interaction.
   * @param interaction interaction the interaction object.
   * @param body body the message body.
   * @throws org.ccsds.moims.mo.mal.MALException if there is a internal error.
   * @throws org.ccsds.moims.mo.mal.MALInteractionException if there is a operation interaction error.
   */
  public void handleProgress(org.ccsds.moims.mo.mal.provider.MALProgress interaction, org.ccsds.moims.mo.mal.transport.MALMessageBody body) throws org.ccsds.moims.mo.mal.MALInteractionException, org.ccsds.moims.mo.mal.MALException
  {
    switch (interaction.getOperation().getNumber().getValue())
    {
      default:
        interaction.sendError(new org.ccsds.moims.mo.mal.MALStandardError(org.ccsds.moims.mo.mal.MALHelper.UNSUPPORTED_OPERATION_ERROR_NUMBER, new org.ccsds.moims.mo.mal.structures.Union("Unknown operation")));
    }
  }

}
