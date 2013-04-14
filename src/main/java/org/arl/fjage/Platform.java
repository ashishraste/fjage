/******************************************************************************

Copyright (c) 2013, Mandar Chitre

This file is part of fjage which is released under Simplified BSD License.
See file LICENSE.txt or go to http://www.opensource.org/licenses/BSD-3-Clause
for full license details.

******************************************************************************/

package org.arl.fjage;

import java.net.*;
import java.util.*;
import java.util.jar.*;

/**
 * Base class for platforms on which agent containers run. The platform provides
 * basic system functionality such as timing, network, etc.
 *
 * @author  Mandar Chitre
 */
public abstract class Platform implements TimestampProvider {

  /////////// Private attributes

  protected List<Container> containers = new ArrayList<Container>();
  private String hostname = null;
  private int port = 1099;
  private NetworkInterface nif = null;

  ////////// Interface methods for platforms to implement

  /**
   * Gets the current platform time in milliseconds. For real-time platforms,
   * this time is epoch time. For simulation platforms, this time is simulation
   * time.
   *
   * @return time in milliseconds.
   */
  @Override
  public abstract long currentTimeMillis();

  /**
   * Gets the current platform time in nanoseconds. For real-time platforms,
   * this time is epoch time. For simulation platforms, this time is simulation
   * time. This time is nanosecond precision, but not necessarily nanosecond accuracy.
   *
   * @return time in nanoseconds.
   */
  @Override
  public abstract long nanoTime();

  /**
   * Schedules a task to be executed at a given platform time.
   *
   * @param task task to be executed.
   * @param millis time at which to execute the task.
   */
  public abstract void schedule(TimerTask task, long millis);

  /**
   * Internal method called by a container when all agents are idle.
   */
  public abstract void idle();

  /**
   * Delays execution by a specified number of milliseconds of platform time.
   *
   * @param millis number of milliseconds to delay execution.
   */
  public abstract void sleep(long millis);

  ////////// Interface methods

  /**
   * Adds a container to the platform. This method is typically called automatically
   * by containers when they are created.
   *
   * @param container the container.
   */
  public void addContainer(Container container) {
    containers.add(container);
  }

  /**
   * Gets all the containers on the platform.
   *
   * @return an array of containers.
   */
  public Container[] getContainers() {
    return containers.toArray(new Container[containers.size()]);
  }

  /**
   * Starts all containers on the platform.
   */
  public void start() {
    for (Container c: containers)
      c.start();
  }

  /**
   * Terminates all containers on the platform.
   */
  public void shutdown() {
    for (Container c: containers) {
      if (c != null) c.shutdown();
    }
  }

  /**
   * Sets the hostname for the platform.
   *
   * @param hostname name of the host.
   * @see #getHostname()
   */
  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  /**
   * Gets the hostname for the platform. If the hostname is not set or set to
   * null, it is automatically determined from the Internet hostname for the
   * machine.
   *
   * @return the name of the host.
   */
  public String getHostname() {
    if (hostname != null) return hostname;
    try {
      InetAddress addr = null;
      if (nif == null) addr = InetAddress.getLocalHost();
      else {
        Enumeration<InetAddress> alist = nif.getInetAddresses();
        addr = alist.nextElement();
        while (addr != null && addr instanceof Inet6Address)
          addr = alist.nextElement();
      }
      if (addr == null) return "localhost";
      return addr.getHostAddress();
    } catch (UnknownHostException ex) {
      return null;
    }
  }

  /**
   * Gets a network interface that the platform is bound to.
   * 
   * @return bound network interface, null if no binding.
   */
  public NetworkInterface getNetworkInterface() {
    return nif;
  }

  /**
   * Sets the network interface to bind to.
   * 
   * @param name name of the network interface.
   */
  public void setNetworkInterface(String name) throws SocketException {
    nif = NetworkInterface.getByName(name);
  }
  
  /**
   * Sets the network interface to bind to.
   * 
   * @param nif network interface.
   */
  public void setNetworkInterface(NetworkInterface nif) {
    this.nif = nif;
  }

  /**
   * Sets the TCP/IP port for platforms supporting remote connections. This
   * defaults to 1099, the Java RMI port.
   *
   * @param port TCP/IP port number.
   * @throws java.lang.UnsupportedOperationException if remote connections are not supported.
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Gets the TCP/IP port for platforms supporting remote connections. This
   * defaults to 1099, the Java RMI port.
   *
   * @return TCP/IP port number
   * @throws java.lang.UnsupportedOperationException if remote connections are not supported.
   */
  public int getPort() {
    return port;
  }

  /**
   * Check if any container on the platform is running.
   *
   * @return true if a container is running, false otherwise.
   */
  public boolean isRunning() {
    for (Container c: containers) {
      if (c != null && c.isRunning()) return true;
    }
    return false;
  }

  /**
   * Get build version information from JAR.
   *
   * @return build version information string.
   */
  public static String getBuildVersion() {
    try {
      Class<?> cls = Platform.class;
      URL res = cls.getResource(cls.getSimpleName() + ".class");
      JarURLConnection conn = (JarURLConnection) res.openConnection();
      Manifest mf = conn.getManifest();
      Attributes a = mf.getMainAttributes();
      return "fjage-"+a.getValue("Build-Version")+"/"+a.getValue("Build-Timestamp");
    } catch (Exception ex) {
      return "(unknown)";
    }
  }

}
