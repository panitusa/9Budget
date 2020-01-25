package com.ninebudget.model;

import com.ninebudget.model.dto.AccountDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

public interface AccountOperations {
    @RequestMapping(value = "/accounts",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<AccountDto>> getAll(Pageable pageable, @RequestParam(required = false) String filter) throws ServiceException;

    @RequestMapping(value = "/accounts/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AccountDto> get(@PathVariable long id) throws ServiceException;

    @RequestMapping(value = "/accounts",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AccountDto> update(@Valid @RequestBody AccountDto account) throws ServiceException;

    @RequestMapping(value = "/accounts",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AccountDto> create(@Valid @RequestBody AccountDto account) throws ServiceException;

    @RequestMapping(value = "/accounts/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> delete(@PathVariable long id) throws ServiceException;
}