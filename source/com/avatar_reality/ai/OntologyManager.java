package com.avatar_reality.ai;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.avatar_reality.ai.BotCommandProcessor;
import com.clarkparsia.owlapi.explanation.PelletExplanation;

public class OntologyManager
{
	private static final String	NS		= "http://ar#";

	static Logger _logger = Logger.getLogger(OntologyManager.class);

	private OWLOntologyManager _owlOntologyManager;
	private OWLDataFactory _owlDataFactory;
	private OWLOntology _ontology;
	private BotCommandProcessor _processor;
	
	public OntologyManager(BotCommandProcessor processor, String ontologyXML)
	{
		_processor = processor;
		_owlOntologyManager = OWLManager.createOWLOntologyManager();
		_owlDataFactory = _owlOntologyManager.getOWLDataFactory();

		try
		{
			InputStream inputStream = new ByteArrayInputStream(ontologyXML.getBytes());
			_ontology = _owlOntologyManager.loadOntologyFromOntologyDocument( inputStream);
//			reasonerFactory = new PelletReasonerFactory();
//			OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(_ontology);
		}
		catch (OWLOntologyCreationException e)
		{
			e.printStackTrace();
		}
	}
	
	public void save()
	{
		System.out.println("Save.");
		try
		{
			OWLOntologyFormat format = new OWLXMLOntologyFormat();
			OutputStream outputStream = new ByteArrayOutputStream();
			_owlOntologyManager.saveOntology(_ontology, format, outputStream);

			// Unfortunate extraction required.
			SAXReader xmlReader = new SAXReader();
			Document xmlDocument;
			String ontologyXML = outputStream.toString();
			_logger.info("Ontology: "+ontologyXML);
			xmlDocument = xmlReader.read(new StringReader(ontologyXML));
			Node ontologyElement = xmlDocument.selectSingleNode("Ontology");
			String queryString = "delete nodes collection()/BOT-L/*:Ontology, insert node "+ontologyElement.asXML()+" into collection()/BOT-L";
			_logger.info("Update ontology: "+queryString);
			_processor.addPostQuery(queryString);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public OWLClass addClass(String className)
	{
        return _owlDataFactory.getOWLClass(IRI.create(NS + className));
	}
	
	public OWLSubClassOfAxiom addSubClass(String subClassName, String superClassName)
	{
        OWLClass subClass = _owlDataFactory.getOWLClass(IRI.create(NS + subClassName));
        OWLClass superClass = _owlDataFactory.getOWLClass(IRI.create(NS + superClassName));
        OWLSubClassOfAxiom subClassAxiom = _owlDataFactory.getOWLSubClassOfAxiom(subClass, superClass);
        _owlOntologyManager.applyChange(new AddAxiom(_ontology, subClassAxiom));
        return subClassAxiom;
	}
	
	public OWLNamedIndividual addIndividual(String name)
	{
        return _owlDataFactory.getOWLNamedIndividual(IRI.create(NS + name));
	}
	
	public OWLClassAssertionAxiom addIndividualInstance(String name, String className)
	{
		OWLNamedIndividual individual = addIndividual(name);
        OWLClass owlClass = addClass(className);
        OWLClassAssertionAxiom instanceAxiom =  _owlDataFactory.getOWLClassAssertionAxiom(owlClass,individual);
        _owlOntologyManager.applyChange(new AddAxiom(_ontology, instanceAxiom));
        return instanceAxiom;
	}

	public OWLObjectProperty addObjectProperty(String propertyName)
	{
        return _owlDataFactory.getOWLObjectProperty(IRI.create(NS + propertyName));
	}
	
	public OWLObjectPropertyAssertionAxiom addObjectProperty(String propertyName, String subjectName, String objectName)
	{
		OWLObjectProperty property = addObjectProperty(propertyName);
		OWLNamedIndividual subject = addIndividual(subjectName);
		OWLNamedIndividual object = addIndividual(objectName);
		OWLObjectPropertyAssertionAxiom propertyAxiom = _owlDataFactory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
        _owlOntologyManager.applyChange(new AddAxiom(_ontology, propertyAxiom));
        return propertyAxiom;
	}
	
	public Set<OWLAxiom> isa(String subClassName, String superClassName)
	{		
        OWLClass subClass = addClass(subClassName);
        OWLClass superClass = addClass(superClassName);
		PelletExplanation expGen = new PelletExplanation(_ontology );
		Set<OWLAxiom> resultSet = expGen.getSubClassExplanation(subClass, superClass);
		if (resultSet.size()==0)
			System.out.println("No.");
		else
			System.out.println("Yes.");
		for (OWLAxiom axiom : resultSet)
		{
			System.out.println("Axiom: "+axiom.toString());
		}
		return resultSet;
	}
}
