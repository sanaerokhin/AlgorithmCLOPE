package org.example.repositories;

import jakarta.transaction.Transactional;
import org.example.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE Transaction t SET t.clusterId = NULL")
    void clearClusterIdValues();

    List<Transaction> findAllByClusterId(int i);

    long countByClusterIdIsNotNull();

    @Query(value = "SELECT t FROM Transaction t ORDER BY t.id ASC")
    Page<Transaction> findTransactions(Pageable pageable);
}
