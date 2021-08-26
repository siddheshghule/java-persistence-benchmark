package de.uniba.dsg.jpb.data.access.jpa;

import de.uniba.dsg.jpb.data.model.jpa.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<WarehouseEntity, String> {}
