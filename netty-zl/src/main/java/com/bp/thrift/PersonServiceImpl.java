package com.bp.thrift;

import org.apache.thrift.TException;
import thrift.generated.DataException;
import thrift.generated.Person;
import thrift.generated.PersonService;

/**
 * Created by ribbo on 6/13/2017.
 */
public class PersonServiceImpl implements PersonService.Iface{
    @Override
    public Person getPersonByUsername(String username) throws DataException, TException {

        System.out.println("getPersonByUsername " + username);
        Person person = new Person();
        person.setUsername(username);
        person.setAge(28);
        person.setMarried(false);
        return person;
    }

    @Override
    public void savePerson(Person person) throws DataException, TException {
        System.out.println("savePerson " + person.getUsername());
        System.out.println(person.toString());
    }
}
