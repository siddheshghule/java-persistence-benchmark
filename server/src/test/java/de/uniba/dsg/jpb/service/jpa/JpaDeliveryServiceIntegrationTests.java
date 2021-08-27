package de.uniba.dsg.jpb.service.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uniba.dsg.jpb.data.access.jpa.CarrierRepository;
import de.uniba.dsg.jpb.data.access.jpa.CustomerRepository;
import de.uniba.dsg.jpb.data.access.jpa.DistrictRepository;
import de.uniba.dsg.jpb.data.access.jpa.OrderRepository;
import de.uniba.dsg.jpb.data.access.jpa.ProductRepository;
import de.uniba.dsg.jpb.data.access.jpa.WarehouseRepository;
import de.uniba.dsg.jpb.data.gen.jpa.JpaDataGenerator;
import de.uniba.dsg.jpb.data.model.jpa.OrderEntity;
import de.uniba.dsg.jpb.data.model.jpa.OrderItemEntity;
import de.uniba.dsg.jpb.data.transfer.messages.DeliveryRequest;
import de.uniba.dsg.jpb.data.transfer.messages.DeliveryResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@DataJpaTest
public class JpaDeliveryServiceIntegrationTests {

  @Autowired private WarehouseRepository warehouseRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private DistrictRepository districtRepository;
  @Autowired private OrderRepository orderRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private CarrierRepository carrierRepository;
  private JpaDeliveryService deliveryService;
  private DeliveryRequest request;
  private String orderId;

  @BeforeEach
  public void setUp() {
    JpaDataGenerator generator =
        new JpaDataGenerator(1, 1, 1, 1, 1_000, new BCryptPasswordEncoder());
    generator.generate();

    productRepository.saveAll(generator.getProducts());
    carrierRepository.saveAll(generator.getCarriers());
    warehouseRepository.saveAll(generator.getWarehouses());

    // Ensure that single order is not fulfilled
    orderId = generator.getWarehouses().get(0).getDistricts().get(0).getOrders().get(0).getId();
    OrderEntity order = orderRepository.getById(orderId);
    order.setCarrier(null);
    order.setFulfilled(false);
    order.getItems().forEach(i -> i.setDeliveryDate(null));
    orderRepository.save(order);

    request = new DeliveryRequest();
    request.setWarehouseId(warehouseRepository.findAll().get(0).getId());
    request.setCarrierId(carrierRepository.findAll().get(0).getId());

    deliveryService =
        new JpaDeliveryService(
            districtRepository, orderRepository, customerRepository, carrierRepository);
  }

  @Test
  public void deliveryProcessingReturnsExpectedValues() {
    DeliveryResponse res = deliveryService.process(request);

    assertEquals(request.getWarehouseId(), res.getWarehouseId());
    assertEquals(request.getCarrierId(), res.getCarrierId());
  }

  @Test
  public void orderIsUpdatedByDeliveryRequest() {
    deliveryService.process(request);

    OrderEntity order = orderRepository.getById(orderId);
    assertTrue(order.isFulfilled());
    assertNotNull(order.getCarrier());
    for (OrderItemEntity item : order.getItems()) {
      assertNotNull(item.getDeliveryDate());
    }
  }

  @AfterEach
  public void tearDown() {
    warehouseRepository.deleteAll();
    productRepository.deleteAll();
    carrierRepository.deleteAll();
  }
}