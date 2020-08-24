package au.org.aodn.nrmn.restapi.repository;

import au.org.aodn.nrmn.restapi.model.db.SurveyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
* Generated by Spring Data Generator on 10/01/2020
*/
@Repository
public interface SurveyEntityRepository extends JpaRepository<SurveyEntity, Integer>, JpaSpecificationExecutor<SurveyEntity> {
    List<SurveyEntity> findAll( );

    Optional<SurveyEntity> findById(Integer id);

    @Query("SELECT t FROM #{#entityName} t WHERE t.id IN :ids")
    List<SurveyEntity> findByIdsIn(@Param("ids") List<Integer> ids);
}
