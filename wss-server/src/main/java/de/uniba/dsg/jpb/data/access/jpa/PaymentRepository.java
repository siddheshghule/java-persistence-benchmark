package de.uniba.dsg.jpb.data.access.jpa;

import de.uniba.dsg.jpb.data.model.jpa.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for accessing and modifying customer {@link PaymentEntity payments}.
 *
 * @author Benedikt Full
 */
@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {}