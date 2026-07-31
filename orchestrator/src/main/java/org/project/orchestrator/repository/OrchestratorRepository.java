package org.project.orchestrator.repository;

import org.project.common.saga.Saga;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrchestratorRepository extends CrudRepository<Saga, Long> {

    public Saga findSagaBySagaId(Long sagaId);
}
