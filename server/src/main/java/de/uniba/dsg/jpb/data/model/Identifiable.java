package de.uniba.dsg.jpb.data.model;

public interface Identifiable<T> {

  T getId();

  void setId(T id);
}
