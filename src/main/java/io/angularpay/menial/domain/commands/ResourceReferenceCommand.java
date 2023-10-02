package io.angularpay.menial.domain.commands;

public interface ResourceReferenceCommand<T, R> {

    R map(T referenceResponse);
}
