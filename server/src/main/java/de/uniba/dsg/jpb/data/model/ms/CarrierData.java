package de.uniba.dsg.jpb.data.model.ms;

import org.jacis.plugin.objectadapter.cloning.JacisCloneable;

/**
 * A carrier is responsible for fulfilling {@link OrderData Orders} by delivering the ordered items
 * to the {@link CustomerData Customer}.
 *
 * @author Benedikt Full
 */
public class CarrierData extends BaseData implements JacisCloneable<CarrierData> {

  private String name;
  private String phoneNumber;
  private AddressData address;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    checkWritable();
    this.name = name;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    checkWritable();
    this.phoneNumber = phoneNumber;
  }

  public AddressData getAddress() {
    return address;
  }

  public void setAddress(AddressData address) {
    checkWritable();
    this.address = address;
  }

  @Override
  public CarrierData clone() {
    return (CarrierData) super.clone();
  }
}
