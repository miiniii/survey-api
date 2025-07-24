package com.api.demo.repository;

import com.api.demo.model.entity.Survey;
import com.api.demo.model.entity.SurveyDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    @Query(value = """
        select
            (select name from company where id = survey.company_id) as company_name,
            (select name from category where id = survey.category_id) as category_name,
            (select sort_order from category where id = survey.category_id) as sort_order,
            survey.*
        from survey where company_id = :companyId
        order by sort_order
    """, nativeQuery = true)
    List<SurveyDto> findSurveyWith(Long companyId);
}

