package de.uniba.dsg.jpb.server.data.gen;

import com.github.javafaker.Faker;
import de.uniba.dsg.jpb.data.model.jpa.AddressEmbeddable;
import de.uniba.dsg.jpb.data.model.jpa.CustomerEntity;
import de.uniba.dsg.jpb.data.model.jpa.DistrictEntity;
import de.uniba.dsg.jpb.data.model.jpa.OrderEntity;
import de.uniba.dsg.jpb.data.model.jpa.OrderItemEntity;
import de.uniba.dsg.jpb.data.model.jpa.PaymentEntity;
import de.uniba.dsg.jpb.data.model.jpa.ProductEntity;
import de.uniba.dsg.jpb.data.model.jpa.StockEntity;
import de.uniba.dsg.jpb.data.model.jpa.WarehouseEntity;
import de.uniba.dsg.jpb.util.UniformRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class JpaDataGenerator {

  private static final String BAD_CREDIT = "BC";
  private static final String GOOD_CREDIT = "GC";
  private static final String ORIGINAL = "ORIGINAL";
  private final int warehouseCount;
  private final int itemCount;
  private final int districtsPerWarehouseCount;
  private final int customersPerDistrictCount;
  private final int ordersPerDistrictCount;
  private final Faker faker;
  private final UniformRandom salesTaxRandom;
  private final UniformRandom oneInThreeRandom;
  private List<ProductEntity> products;
  private List<WarehouseEntity> warehouses;

  public JpaDataGenerator(int warehouseCount, boolean limited) {
    faker = new Faker(Locale.US);
    salesTaxRandom = new UniformRandom(0.0, 0.2, 1);
    oneInThreeRandom = new UniformRandom(1, 3);
    this.warehouseCount = warehouseCount;
    if (limited) {
      itemCount = 1_000;
      districtsPerWarehouseCount = 10;
      customersPerDistrictCount = 30;
      ordersPerDistrictCount = 30;
    } else {
      itemCount = 100_000;
      districtsPerWarehouseCount = 10;
      customersPerDistrictCount = 3_000;
      ordersPerDistrictCount = 3_000;
    }
    products = null;
    warehouses = null;
  }

  public List<WarehouseEntity> getWarehouses() {
    return warehouses;
  }

  public List<ProductEntity> getProducts() {
    return products;
  }

  public void generate() {
    products = generateProducts();
    warehouses = generateWarehouses();
  }

  private List<ProductEntity> generateProducts() {
    UniformRandom priceRandom = new UniformRandom(1.0, 100.0, 2);
    UniformRandom imageIdRandom = new UniformRandom(1_000_000, 5_000_000);
    List<ProductEntity> items = new ArrayList<>(itemCount);
    for (int i = 0; i < itemCount; i++) {
      ProductEntity product = new ProductEntity();
      product.setImageId(imageIdRandom.nextLong());
      product.setName(faker.commerce().productName());
      if (i % 10_000 == 0) {
        product.setData(insertOriginal(lorem26To50()));
      } else {
        product.setData(lorem26To50());
      }
      product.setPrice(priceRandom.nextDouble());
      items.add(product);
    }
    return items;
  }

  private List<WarehouseEntity> generateWarehouses() {
    List<WarehouseEntity> warehouses = new ArrayList<>(warehouseCount);
    List<AddressEmbeddable> addresses = generateAddresses(warehouseCount);
    for (int i = 0; i < warehouseCount; i++) {
      WarehouseEntity warehouse = new WarehouseEntity();
      warehouse.setName(faker.address().cityName());
      warehouse.setAddress(addresses.get(i));
      warehouse.setSalesTax(salesTaxRandom.nextDouble());
      warehouse.setYearToDateBalance(300_000);
      warehouse.setDistricts(generateDistricts(warehouse));
      warehouse.setStocks(generateStocks(warehouse, products));
      warehouses.add(warehouse);
    }
    return warehouses;
  }

  private List<DistrictEntity> generateDistricts(WarehouseEntity warehouse) {
    List<DistrictEntity> districts = new ArrayList<>(10);
    List<AddressEmbeddable> addresses = generateAddresses(districtsPerWarehouseCount);
    for (int i = 0; i < districtsPerWarehouseCount; i++) {
      DistrictEntity district = new DistrictEntity();
      district.setWarehouse(warehouse);
      districts.add(district);
      district.setName(faker.address().cityName());
      district.setAddress(addresses.get(i));
      district.setSalesTax(salesTaxRandom.nextDouble());
      district.setYearToDateBalance(30_000);
      district.setCustomers(generateCustomers(district));
      district.setOrders(generateOrders(district, products));
    }
    return districts;
  }

  private List<CustomerEntity> generateCustomers(DistrictEntity district) {
    List<CustomerEntity> customers = new ArrayList<>(customersPerDistrictCount);
    List<AddressEmbeddable> addresses = generateAddresses(customersPerDistrictCount);
    UniformRandom discountRandom = new UniformRandom(0.0, 0.5, 2);
    UniformRandom creditRandom = new UniformRandom(1, 100);
    for (int i = 0; i < customersPerDistrictCount; i++) {
      CustomerEntity customer = new CustomerEntity();
      customer.setDistrict(district);
      customer.setAddress(addresses.get(i));
      customer.setFirstName(faker.name().firstName());
      customer.setMiddleName(faker.name().firstName());
      customer.setLastName(faker.name().lastName());
      customer.setPhoneNumber(faker.phoneNumber().phoneNumber());
      customer.setSince(LocalDateTime.now());
      customer.setPayments(List.of(generatePayment(customer)));
      customer.setCredit(creditRandom.nextInt() < 11 ? BAD_CREDIT : GOOD_CREDIT);
      customer.setCreditLimit(50_000);
      customer.setDiscount(discountRandom.nextDouble());
      customer.setBalance(-10.0);
      customer.setYearToDatePayment(10.0);
      customer.setPaymentCount(1);
      customer.setDeliveryCount(0);
      customer.setData(lorem(300, 500));
      customers.add(customer);
    }
    return customers;
  }

  private PaymentEntity generatePayment(CustomerEntity customer) {
    PaymentEntity payment = new PaymentEntity();
    payment.setCustomer(customer);
    payment.setDistrict(customer.getDistrict());
    payment.setDate(LocalDateTime.now());
    payment.setAmount(10.0);
    payment.setData(lorem26To50());
    return payment;
  }

  private List<StockEntity> generateStocks(
      WarehouseEntity warehouse, List<ProductEntity> products) {
    List<StockEntity> stocks = new ArrayList<>(products.size());
    final int length = 24;
    UniformRandom quantityRandom = new UniformRandom(10, 100);
    for (ProductEntity product : products) {
      StockEntity stock = new StockEntity();
      stock.setProduct(product);
      stock.setWarehouse(warehouse);
      stock.setDist01(loremFixedLength(length));
      stock.setDist02(loremFixedLength(length));
      stock.setDist03(loremFixedLength(length));
      stock.setDist04(loremFixedLength(length));
      stock.setDist05(loremFixedLength(length));
      stock.setDist06(loremFixedLength(length));
      stock.setDist07(loremFixedLength(length));
      stock.setDist08(loremFixedLength(length));
      stock.setDist09(loremFixedLength(length));
      stock.setDist10(loremFixedLength(length));
      stock.setData(lorem26To50());
      stock.setOrderCount(0);
      stock.setRemoteCount(0);
      stock.setQuantity(quantityRandom.nextInt());
      stocks.add(stock);
    }
    return stocks;
  }

  private List<AddressEmbeddable> generateAddresses(int count) {
    List<AddressEmbeddable> addresses = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      AddressEmbeddable address = new AddressEmbeddable();
      address.setStreet1(faker.address().streetAddress());
      address.setStreet2(faker.address().secondaryAddress());
      address.setCity(faker.address().cityName());
      address.setState(faker.address().stateAbbr());
      address.setZipCode(faker.address().zipCodeByState(address.getState()));
      addresses.add(address);
    }
    return addresses;
  }

  private List<OrderEntity> generateOrders(DistrictEntity district, List<ProductEntity> products) {
    if (district.getCustomers() == null
        || district.getCustomers().size() != customersPerDistrictCount
        || products.size() != itemCount) {
      throw new IllegalArgumentException();
    }
    List<OrderEntity> orders = new ArrayList<>(ordersPerDistrictCount);
    List<CustomerEntity> shuffledCustomers = new ArrayList<>(district.getCustomers());
    Collections.shuffle(shuffledCustomers);
    UniformRandom carrierIdRandom = new UniformRandom(1, 10);
    UniformRandom orderItemCountRandom = new UniformRandom(5, 15);
    for (int i = 0; i < ordersPerDistrictCount; i++) {
      CustomerEntity customer = shuffledCustomers.get(i);
      OrderEntity order = new OrderEntity();
      order.setCustomer(customer);
      order.setDistrict(customer.getDistrict());
      order.setEntryDate(LocalDateTime.now());
      order.setCarrierId(oneInThreeRandom.nextInt() < 3 ? carrierIdRandom.nextLong() : null);
      order.setItemCount(orderItemCountRandom.nextInt());
      order.setAllLocal(true);
      order.setItems(generateOrderItems(order, products));
      orders.add(order);
    }
    return orders;
  }

  private List<OrderItemEntity> generateOrderItems(
      OrderEntity order, List<ProductEntity> products) {
    if (products.size() != itemCount) {
      throw new IllegalArgumentException();
    }
    List<OrderItemEntity> orderItems = new ArrayList<>(order.getItemCount());
    UniformRandom itemIdxRandom = new UniformRandom(0, products.size() - 1);
    UniformRandom amountRandom = new UniformRandom(0.01, 9_999.9, 2);
    for (int i = 0; i < order.getItemCount(); i++) {
      OrderItemEntity orderItem = new OrderItemEntity();
      orderItem.setOrder(order);
      orderItem.setNumber(i + 1);
      orderItem.setProduct(products.get(itemIdxRandom.nextInt()));
      orderItem.setSupplyingWarehouse(order.getDistrict().getWarehouse());
      orderItem.setDeliveryDate(oneInThreeRandom.nextInt() < 3 ? order.getEntryDate() : null);
      orderItem.setQuantity(5);
      orderItem.setAmount(oneInThreeRandom.nextInt() < 3 ? 0.0 : amountRandom.nextDouble());
      orderItem.setDistInfo(loremFixedLength(24));
      orderItems.add(orderItem);
    }
    return orderItems;
  }

  private String insertOriginal(String s) {
    if (s.length() < 26 || s.length() > 50) {
      throw new IllegalArgumentException();
    }
    UniformRandom indexRandom = new UniformRandom(0, 17);
    int index = indexRandom.nextInt();
    return s.substring(0, index) + ORIGINAL + s.substring(index + ORIGINAL.length());
  }

  private String loremFixedLength(int length) {
    return faker.lorem().characters(length);
  }

  private String lorem(int minimumLength, int maximumLength) {
    return faker.lorem().characters(minimumLength, maximumLength, true, true);
  }

  private String lorem26To50() {
    return lorem(26, 50);
  }
}