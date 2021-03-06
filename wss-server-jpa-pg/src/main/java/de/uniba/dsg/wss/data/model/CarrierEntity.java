package de.uniba.dsg.wss.data.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * A carrier is responsible for fulfilling {@link OrderEntity orders} by delivering the ordered
 * items to the {@link CustomerEntity customer}.
 *
 * @author Benedikt Full
 */
@Entity(name = "Carrier")
@Table(name = "carriers")
public class CarrierEntity extends BaseEntity {

  @Column(unique = true, nullable = false)
  private String name;

  @Column(nullable = false)
  private String phoneNumber;

  @Embedded private AddressEmbeddable address;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public AddressEmbeddable getAddress() {
    return address;
  }

  public void setAddress(AddressEmbeddable address) {
    this.address = address;
  }
}
