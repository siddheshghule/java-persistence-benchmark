package de.uniba.dsg.jpb.service.jpa;

import de.uniba.dsg.jpb.data.access.jpa.CarrierRepository;
import de.uniba.dsg.jpb.data.access.jpa.CustomerRepository;
import de.uniba.dsg.jpb.data.access.jpa.DistrictRepository;
import de.uniba.dsg.jpb.data.access.jpa.OrderRepository;
import de.uniba.dsg.jpb.data.model.jpa.CarrierEntity;
import de.uniba.dsg.jpb.data.model.jpa.CustomerEntity;
import de.uniba.dsg.jpb.data.model.jpa.DistrictEntity;
import de.uniba.dsg.jpb.data.model.jpa.OrderEntity;
import de.uniba.dsg.jpb.data.model.jpa.OrderItemEntity;
import de.uniba.dsg.jpb.data.transfer.messages.DeliveryRequest;
import de.uniba.dsg.jpb.data.transfer.messages.DeliveryResponse;
import de.uniba.dsg.jpb.service.DeliveryService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "jpb.persistence.mode", havingValue = "jpa")
public class JpaDeliveryService extends DeliveryService {

  private final DistrictRepository districtRepository;
  private final OrderRepository orderRepository;
  private final CustomerRepository customerRepository;
  private final CarrierRepository carrierRepository;

  @Autowired
  public JpaDeliveryService(
      DistrictRepository districtRepository,
      OrderRepository orderRepository,
      CustomerRepository customerRepository,
      CarrierRepository carrierRepository) {
    this.districtRepository = districtRepository;
    this.orderRepository = orderRepository;
    this.customerRepository = customerRepository;
    this.carrierRepository = carrierRepository;
  }

  @Transactional(isolation = Isolation.SERIALIZABLE)
  @Override
  public DeliveryResponse process(DeliveryRequest req) {
    // Find districts and carrier
    List<DistrictEntity> districts = districtRepository.findByWarehouseId(req.getWarehouseId());
    CarrierEntity carrier = carrierRepository.getById(req.getCarrierId());

    for (DistrictEntity district : districts) {
      double amountSum = 0;
      // Find oldest new/unfulfilled order
      OrderEntity order =
          orderRepository.findOldestUnfulfilledOrderOfDistrict(district.getId()).orElse(null);
      if (order == null) {
        // No unfulfilled orders for this district, do nothing
        continue;
      }

      // Update fulfillment status and carrier of order
      order.setCarrier(carrier);
      order.setFulfilled(true);

      // Find all order items, set delivery date to now and sum amount
      for (OrderItemEntity orderItem : order.getItems()) {
        orderItem.setDeliveryDate(LocalDateTime.now());
        amountSum += orderItem.getAmount();
      }
      // Save order and items
      order = orderRepository.save(order);

      // Update customer balance and delivery count
      CustomerEntity customer = order.getCustomer();
      customer.setBalance(customer.getBalance() + amountSum);
      customer.setDeliveryCount(customer.getDeliveryCount() + 1);
      customerRepository.save(customer);
    }
    return new DeliveryResponse(req);
  }
}