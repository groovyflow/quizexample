package org.tassoni.quizexample.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

//Chuck
public class HibernateAwareObjectMapper extends ObjectMapper{
	
	private static final long serialVersionUID = -2922228569128859604L;

	public HibernateAwareObjectMapper() {
        registerModule(new Hibernate4Module());
    }

}
