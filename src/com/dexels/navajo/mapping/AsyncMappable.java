package com.dexels.navajo.mapping;

import com.dexels.navajo.server.*;
import com.dexels.navajo.document.*;
import java.util.HashMap;

/**
 * <p>Title: Navajo Product Project</p>
 * <p>Description: This is the official source for the Navajo server</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Dexels BV</p>
 * @author Arjen Schoneveld
 * @version $Id$
 *
 * AsyncMappable objects seperate the request and response part of the object's attributes explicitly by asynchronous action.
 *
 * Navajo services that access AsyncMappable objects ALWAYS need two service requests:
 * 1. Initiate "Object request" request.
 * 2. Initiate "Object response" request.
 * (3. Initiate "Object while-running" request).
 * The last service request block is optional.
 *
 * Though these three requests are encapsulated in a single Navascript file. The Navascript semantics changes are minimal in order
 * to support asynchronous objects: three additional tags are defined, which may ONLY occur directly as children of a <map object=""> tag:
 * - <response> (addtional attribute: "while_running"="true"|"false")
 * - <request>
 * - <running>
 *
 * The semantic construct to support this is as follows:
 *
 * <map name="unique name, identifying the object instance" object="com.dexels.navajo.adapter.AsyncClass">
 *   <request>
 *    (Sychronous call to object methods, identical to the standard script)
 *   </request> (-> the runtime will automatically call the method runThread() )
 *   <response while_running="true"> (-> the runtime will call isFinished(), if isFinished returns true, the methods inside the <response> will be executed, if false
 *               a not_finished notification will be send back. If an Exception occured during thread execution, the exception will be thrown
 *               upon calling isFinished(), the runtime will call, as normally, the method kill(). The "while_running" attribute is used to specify whether
 *               the <response> block should be interpreted while the thread is running, the thread will be interrupted while the <response> block is
 *               evaluated! ).
 *     (Use standard script semantics)
 *   </response)
 *   Optionallly the <running> tag can be used to use a "running" thread. Note that the thread will be interrupted though before the <running>
 *   block is interpreted.
 *   <running> (-> the runtime will call interrupt() on the requested Async map)
 *   </running> (-> the runtime will call resume() on the requested Async map)
 * </map> (-> the runtime will automatically call the method store() )
 *
 * If a service contains async object, the server will have to supply the client with proper callback pointers for subsequent requests.
 * These callback pointer will be returned as follows:
 * <header>
 *   <callback>
 *     <object name="unique name" ref="pointer" finished="false" perc_ready="0"/>
 *     <object name="unique name" ref="pointer" finished="false" perc_ready="0"/>
 *   </callback>
 * </header>
 *
 * The client will have to send back the <callback> tags with the attributes "name" and "ref" in order to re-use the object.
 * Additionally an "intterupt" attribute can be specified: kill, interrupt, resume:
 *
 * <callback>
 *   <object name="myObject" ref="3423432" interrupt="kill"/>
 * </callback
 *
 * FUTURE ENHANCEMENT (automatic client callback):
 * The client must run a Navajo server by itself in order to support client callback.
 *
 * <header>
 *   <transaction rpc_name="" rpc_pwd="" rpc_usr="">
 *     <callback url="http://231.32.123.12/myclient/Servlet/Postman" username="AAP" password="NOOT" service="ProcessCallback"/>
 *     [multiple callback locations can be specified]
 *   </transaction>
 *
 * The <response>/<running> part of the async <map> will be send back to the client.
 *
 * The client will need to send the <callback> back in it's subsequent requests.
 *
 */

public abstract class AsyncMappable implements Mappable {

  public boolean isFinished = false;
  private Exception caught = null;
  private long startTime;
  private long lastAccess;
  public String name;
  public String pointer;
  public java.util.Date startDate;

  private RequestThread myRequest = null;

  /**
   * Four different thread states:
   */
  public boolean running = false;
  private boolean stop = false;
  public boolean interrupt = false;
  private boolean resume = false;

  /**
   * This class implements the asynchronous thread.
   * It runs the run() method of the parent object that instantiates this class.
   * After the thread is finished, the thread instance calls the setIsFinished() method
   * of it's parent to indicate the parent's finalization.
   * Upon an exception, the parent's setException() method is called and the exception instance is passed.
   */
  class RequestThread extends Thread {

    private AsyncMappable parent;

    public RequestThread(AsyncMappable parent) {
      this.parent = parent;
    }

    public void run() {
      try {
        parent.run();
      } catch (Exception e) {
        e.printStackTrace();
        parent.setException(e);
      }
      System.out.println("Calling setIsFinished()");
      parent.setIsFinished();
    }

  }

  /**
   * Following three abstract method are required by the Mappable interface.
   * Note that the load() method is ONLY called upon object instantiation, not on subsequent requests.
   * Note that the store() method is ONLY called upon thread finalization and after the final request by the client has been handled.
   *
   */
  public abstract void kill();
  public abstract void store() throws MappableException, UserException;
  public abstract void load(Parameters parms, Navajo inMessage, Access access, NavajoConfig config) throws MappableException, UserException;
  public abstract int getPercReady()  throws UserException;
  /**
   * The method afterRequest() is executed right after the </request> tag.
   */
  public abstract void afterRequest() throws UserException;
  /**
   * The method beforeResponse() is excecuted right before the <response> tag.
   */
  public abstract void beforeResponse(Parameters parms, Navajo inMessage, Access access, NavajoConfig config)  throws UserException;
  /**
   * The method afterResponse() is executed right after the </response> tag.
   */
  public abstract void afterResponse()  throws UserException;

  /**
   * Use this method to let a thread sleep for a while...
   */
  public void goToSleep() {
      System.out.println("GOING TO SLEEP....");
      try {
        while (true) {
          Thread.sleep(3000);
        }
      } catch (java.lang.InterruptedException ie) {
        System.out.println("...WOKE UP!!!");
      }
  }

  public final boolean isInterrupted() {
    return interrupt;
  }

  public void interrupt() {
    interrupt = true;
    resume = false;
  }

  public final boolean isResumed() {
    return resume;
  }

  public void resume() {
    resume = true;
    interrupt = false;
    // Send interrupt!
    myRequest.interrupt();
  }

  public final boolean isStopped() {
    return stop;
  }

  public void stop() {
    System.out.println("stop() called...waiting for thread to terminate...");
    stop = true;
    try {
      myRequest.join(1000);
    } catch (java.lang.InterruptedException ie) {

    }
    System.out.println("calling kill().");
    kill();
  }

  public void finalize() {
    System.out.println("FINALIZE() METHOD CALL FOR OBJECT " + this);
  }

  /**
   * Method to set upon Exception.
   *
   * @param e
   */
  protected void setException(Exception e) {
    caught = e;
  }


  /**
   *
   * This method should by called directly after the load() method.
   *
   * @param name
   * @param pointer
   */
  public void afterReload(String name, String pointer) {
    this.name = name;
    this.pointer = pointer;
  }

  public boolean isActivated() {
    return running;
  }

  /**
   * This method should be called each time a request is made to the async object.
   *
   */
  public void runThread() {
    this.lastAccess = System.currentTimeMillis();
    if (!running) {
      startTime = System.currentTimeMillis();
      System.out.println("STARTED RUNTHREAD ON: " + startTime);
      myRequest = new RequestThread(this);
      myRequest.start();
      running = true;
      stop = interrupt = resume = false;
    }
  }

  /**
   * Run the request thread.
   * This method should implement all the stuff that needs to be done asynchronously.
   *
   * Programmer should implement isInterrupted() and isStopped() properly in order to support those intterupts.
   * method goToSleep() can be used to let a thread to sleep and to have it waken up automatically upon a "resume" interrupt.
   */
  public abstract void run() throws UserException;

  /**
   * Call to check whether the asynchronous thread has finished.
   * Pass the OUTGOING Navajo object and the Access object. A callback header will be added
   * to the Navajo object. The "finished" attribute will be set to true if the thread has finished, false if not.
   *
   * @param inMessage
   * @param access
   * @return
   * @throws Exception
   */
  public synchronized boolean isFinished(Navajo inMessage, Access access) throws Exception {

    Header h = inMessage.getHeader();
    if (h == null) {
        if (access != null)
          h = NavajoFactory.getInstance().createHeader(inMessage, access.rpcName, access.rpcUser, "", -1);
        else
          h = NavajoFactory.getInstance().createHeader(inMessage, "", "", "", -1);
        inMessage.addHeader(h);
    }
    System.out.println(inMessage.toString());
    if (isFinished) {
      System.out.println("THREAD IS FINISHED...");
      if (caught != null)
        throw caught;
      h.setCallBack(this.name, this.pointer, getPercReady(), true, "");
    } else
      h.setCallBack(this.name, this.pointer, getPercReady(), false, "");

    return isFinished;
  }

  /**
   * Used by the thread to indicate that is has finished it's parent's run() method.
   *
   */
  protected void setIsFinished() {
    isFinished = true;
  }

  /**
   * @return the start time of the object (in millis)
   */
  public long getStartTime() {
    return startTime;
  }

  public java.util.Date getStartDate() {
    return new java.util.Date(startTime);
  }

  public String  getName() {
    return this.name;
  }

  public boolean getRunning() {
    return this.running;
  }

  public boolean getIsFinished() {
    return this.isFinished;
  }

  public boolean getInterrupt() {
    return this.interrupt;
  }
  /**
   *
   * @return the last time the object has been accessed (in millis)
   */
  public long getLastAccess() {
    return this.lastAccess;
  }
}