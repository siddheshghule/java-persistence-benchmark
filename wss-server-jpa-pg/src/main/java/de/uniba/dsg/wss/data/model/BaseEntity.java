package de.uniba.dsg.wss.data.model;

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

/**
 * The base class for all JPA entities. It defines the identifier for the object, which is a UUID
 * and a version field for optimistic locking. Furthermore, object equality and hashcode are defined
 * as functions of the identity.
 *
 * @author Benedikt Full
 */
@MappedSuperclass
public abstract class BaseEntity {

  @Id private String id;

  @Version private Long version;

  public BaseEntity() {
    id = UUID.randomUUID().toString();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BaseEntity)) {
      return false;
    }
    BaseEntity other = (BaseEntity) obj;
    return getId().equals(other.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
