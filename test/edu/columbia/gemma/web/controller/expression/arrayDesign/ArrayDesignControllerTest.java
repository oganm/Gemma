package edu.columbia.gemma.web.controller.expression.arrayDesign;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.ModelAndView;

import edu.columbia.gemma.BaseControllerTestCase;
import edu.columbia.gemma.expression.arrayDesign.ArrayDesign;
import edu.columbia.gemma.expression.arrayDesign.ArrayDesignService;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 - 2005 Columbia University
 * 
 * @author keshav
 * @version $Id$
 */
public class ArrayDesignControllerTest extends BaseControllerTestCase {

    private MockServletContext mockCtx;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    ArrayDesignController arrayDesignController;

    ArrayDesign testArrayDesign;

    ArrayDesignService arrayDesignService;

    public void setUp() throws Exception {

        mockCtx = new MockServletContext();

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        arrayDesignController = ( ArrayDesignController ) ctx.getBean( "arrayDesignController" );

        testArrayDesign = ArrayDesign.Factory.newInstance();

        arrayDesignService = ( ArrayDesignService ) ctx.getBean( "arrayDesignService" );

    }

    /**
     * Tear down objects.
     */
    public void tearDown() {
        arrayDesignController = null;
    }

    /**
     * @throws Exception
     */
    public void testGetArrayDesigns() throws Exception {
        ArrayDesignController a = ( ArrayDesignController ) ctx.getBean( "arrayDesignController" );
        ModelAndView mav = a.handleRequest( ( HttpServletRequest ) null, ( HttpServletResponse ) null );
        Collection<ArrayDesign> c = (mav.getModel()).values();
        assertNotNull(c);
        assertEquals( mav.getViewName(), "arrayDesign.GetAll.results.view" );
    }
}
