package com.ninebudget.model;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

public interface BudgetOperations {
    @RequestMapping(value = "/budgets",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    List<Budget> getAll() throws ServiceException;

    @RequestMapping(value = "/budgets/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    Budget get(@PathVariable int id) throws ServiceException;

    @RequestMapping(value = "/budgets",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    void update(@Valid @RequestBody Budget budget) throws ServiceException;

    @RequestMapping(value = "/budgets",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    void create(@Valid @RequestBody Budget budget) throws ServiceException;

    @RequestMapping(value = "/budgets/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    void delete(@PathVariable int id) throws ServiceException;
}
