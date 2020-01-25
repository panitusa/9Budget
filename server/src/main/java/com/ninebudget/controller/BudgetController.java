package com.ninebudget.controller;

import com.ninebudget.model.APIController;
import com.ninebudget.model.BudgetOperations;
import com.ninebudget.model.ServiceException;
import com.ninebudget.model.dto.BudgetDto;
import com.ninebudget.service.BudgetService;
import com.ninebudget.util.ResponseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@APIController
public class BudgetController implements BudgetOperations {
    private static final Logger log = LogManager.getLogger(BudgetController.class);

    @Autowired
    private BudgetService budgetService;

    @Override
    public ResponseEntity<List<BudgetDto>> getAll() throws ServiceException {
        log.debug("REST request to get all Budgets");

        List<BudgetDto> page = budgetService.findAll();

        return ResponseEntity.ok().body(page);
    }

    @Override
    public ResponseEntity<BudgetDto> get(long id) throws ServiceException {
        log.debug("REST request to get Budget : {}", id);

        Optional<BudgetDto> budgetDto = budgetService.findOne(id);

        return ResponseUtil.wrapOrNotFound(budgetDto);
    }

    @Override
    public ResponseEntity<BudgetDto> update(BudgetDto budget) throws ServiceException {
        log.debug("REST request to update Budget : {}", budget);

        BudgetDto result = budgetService.save(budget);

        URI uri;
        try{
            uri = new URI(String.valueOf(result.getId()));
        } catch (URISyntaxException e) {
            throw new ServiceException(e);
        }

        return ResponseEntity.created(uri).body(result);
    }

    @Override
    public ResponseEntity<BudgetDto> create(BudgetDto budget) throws ServiceException {
        log.debug("REST request to create Budget : {}", budget);

        BudgetDto result = budgetService.save(budget);

        URI uri;
        try{
            uri = new URI(String.valueOf(result.getId()));
        } catch (URISyntaxException e) {
            throw new ServiceException(e);
        }

        return ResponseEntity.created(uri).body(result);
    }

    @Override
    public ResponseEntity<Void> delete(long id) throws ServiceException {
        log.debug("REST request to delete Budget : {}", id);

        budgetService.delete(id);

        return ResponseEntity.noContent().build();
    }
}