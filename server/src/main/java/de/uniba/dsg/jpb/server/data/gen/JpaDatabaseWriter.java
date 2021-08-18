package de.uniba.dsg.jpb.server.data.gen;

import de.uniba.dsg.jpb.server.data.access.jpa.CarrierRepository;
import de.uniba.dsg.jpb.server.data.access.jpa.EmployeeRepository;
import de.uniba.dsg.jpb.server.data.access.jpa.ProductRepository;
import de.uniba.dsg.jpb.server.data.access.jpa.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "jpb.persistence.mode", havingValue = "jpa")
public class JpaDatabaseWriter {

  private final ProductRepository productRepository;
  private final CarrierRepository carrierRepository;
  private final WarehouseRepository warehouseRepository;
  private final EmployeeRepository employeeRepository;

  @Autowired
  public JpaDatabaseWriter(
      ProductRepository productRepository,
      CarrierRepository carrierRepository,
      WarehouseRepository warehouseRepository,
      EmployeeRepository employeeRepository) {
    this.productRepository = productRepository;
    this.carrierRepository = carrierRepository;
    this.warehouseRepository = warehouseRepository;
    this.employeeRepository = employeeRepository;
  }

  public void writeAll(JpaDataGenerator generator) {
    productRepository.saveAll(generator.getProducts());
    carrierRepository.saveAll(generator.getCarriers());
    warehouseRepository.saveAll(generator.getWarehouses());
    employeeRepository.saveAll(generator.getEmployees());
  }
}
