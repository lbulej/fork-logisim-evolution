/*
 * This file is part of logisim-evolution.
 *
 * Logisim-evolution is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Logisim-evolution is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with logisim-evolution. If not, see <http://www.gnu.org/licenses/>.
 *
 * Original code by Carl Burch (http://www.cburch.com), 2011.
 * Subsequent modifications by:
 *   + College of the Holy Cross
 *     http://www.holycross.edu
 *   + Haute École Spécialisée Bernoise/Berner Fachhochschule
 *     http://www.bfh.ch
 *   + Haute École du paysage, d'ingénierie et d'architecture de Genève
 *     http://hepia.hesge.ch/
 *   + Haute École d'Ingénierie et de Gestion du Canton de Vaud
 *     http://www.heig-vd.ch/
 */

package com.cburch.logisim.fpga.designrulecheck;

import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.data.Location;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Net {
  private final Set<Location> myPoints = new HashSet<>();
  private final Set<String> tunnelNames = new HashSet<>();
  private final Set<Wire> segments = new HashSet<>();
  private int nrOfBits;
  private Net myParent;
  private Boolean requiresToBeRoot;
  private final ArrayList<Byte> inheritedBits = new ArrayList<>();
  private final ArrayList<ConnectionPointArray> sourceList = new ArrayList<>();
  private final ArrayList<ConnectionPointArray> sinkList = new ArrayList<>();
  private final ArrayList<ConnectionPointArray> sourceNetsList = new ArrayList<>();
  private final ArrayList<ConnectionPointArray> sinkNetsList = new ArrayList<>();

  public Net() {
    cleanup();
  }

  public Net(Location loc) {
    cleanup();
    myPoints.add(loc);
  }

  public Net(Location loc, int width) {
    cleanup();
    myPoints.add(loc);
    nrOfBits = width;
  }

  public void add(Wire segment) {
    myPoints.add(segment.getEnd0());
    myPoints.add(segment.getEnd1());
    segments.add(segment);
  }

  public Set<Wire> getWires() {
    return segments;
  }

  public boolean AddParentBit(byte bitId) {
    if (bitId < 0) return false;
    inheritedBits.add(bitId);
    return true;
  }

  public boolean addSink(int bitIndex, ConnectionPoint sink) {
    if ((bitIndex < 0) || (bitIndex >= sinkList.size())) return false;
    sinkList.get(bitIndex).AddConnection(sink);
    return true;
  }

  public boolean addSinkNet(int bitIndex, ConnectionPoint sinkNet) {
    if ((bitIndex < 0) || (bitIndex >= sinkNetsList.size())) return false;
    sinkNetsList.get(bitIndex).AddConnection(sinkNet);
    return true;
  }

  public boolean addSource(int bitIndex, ConnectionPoint source) {
    if ((bitIndex < 0) || (bitIndex >= sourceList.size())) return false;
    sourceList.get(bitIndex).AddConnection(source);
    return true;
  }

  public boolean addSourceNet(int bitIndex, ConnectionPoint sourceNet) {
    if ((bitIndex < 0) || (bitIndex >= sourceNetsList.size())) return false;
    sourceNetsList.get(bitIndex).AddConnection(sourceNet);
    return true;
  }

  public void addTunnel(String tunnelName) {
    tunnelNames.add(tunnelName);
  }

  public int BitWidth() {
    return nrOfBits;
  }

  private void cleanup() {
    myPoints.clear();
    segments.clear();
    tunnelNames.clear();
    nrOfBits = 0;
    myParent = null;
    requiresToBeRoot = false;
    inheritedBits.clear();
    sourceList.clear();
    sinkList.clear();
    sourceNetsList.clear();
    sinkNetsList.clear();
  }

  public boolean contains(Location point) {
    return myPoints.contains(point);
  }

  public boolean ContainsTunnel(String tunnelName) {
    return tunnelNames.contains(tunnelName);
  }

  public void ForceRootNet() {
    myParent = null;
    requiresToBeRoot = true;
    inheritedBits.clear();
  }

  public byte getBit(byte bit) {
    if ((bit < 0) || (bit >= inheritedBits.size()) || isRootNet()) return -1;
    return inheritedBits.get(bit);
  }

  public Net getParent() {
    return myParent;
  }

  public Set<Location> getPoints() {
    return this.myPoints;
  }

  public ArrayList<ConnectionPoint> getSinkNets(int bitIndex) {
    if ((bitIndex < 0) || (bitIndex >= sinkNetsList.size()))
      return new ArrayList<>();
    return sinkNetsList.get(bitIndex).GetConnections();
  }

  public ArrayList<ConnectionPoint> getSourceNets(int bitIndex) {
    if ((bitIndex < 0) || (bitIndex >= sourceNetsList.size()))
      return new ArrayList<>();
    return sourceNetsList.get(bitIndex).GetConnections();
  }

  public void cleanupSourceNets(int bitIndex) {
    if ((bitIndex < 0) || (bitIndex >= sourceNetsList.size())) return;
    ArrayList<ConnectionPoint> oldconns = sourceNetsList.get(bitIndex).GetConnections();
    if (oldconns.size() > 1) {
      ConnectionPoint point = oldconns.get(0);
      sourceNetsList.get(bitIndex).ClearConnections();
      sourceNetsList.get(bitIndex).AddConnection(point);
    }
    return;
  }

  public boolean hasBitSinks(int bitid) {
    if (bitid < 0 || bitid >= sinkList.size()) return false;
    return sinkList.get(bitid).NrOfConnections() > 0;
  }

  public ArrayList<ConnectionPoint> getBitSinks(int bitIndex) {
    if ((bitIndex < 0) || (bitIndex >= sourceNetsList.size()))
      return new ArrayList<>();
    return new ArrayList<>(sinkList.get(bitIndex).GetConnections());
  }

  public ArrayList<ConnectionPoint> GetBitSources(int bitIndex) {
    if ((bitIndex < 0) || (bitIndex >= sourceNetsList.size())) return null;
    return sourceList.get(bitIndex).GetConnections();
  }

  public boolean hasBitSource(int bitid) {
    if (bitid < 0 || bitid >= sourceList.size()) return false;
    return sourceList.get(bitid).NrOfConnections() > 0;
  }

  public boolean hasShortCircuit() {
    var ret = false;
    for (var i = 0; i < nrOfBits; i++) ret |= sourceList.get(i).NrOfConnections() > 1;
    return ret;
  }

  public boolean hasSinks() {
    var ret = false;
    for (var i = 0; i < nrOfBits; i++) ret |= sinkList.get(i).NrOfConnections() > 0;
    return ret;
  }

  public Set<ConnectionPoint> GetSinks() {
    final var sinks = new HashSet<ConnectionPoint>();
    for (var i = 0; i < nrOfBits; i++) {
      sinks.addAll(sinkList.get(i).GetConnections());
    }
    return sinks;
  }

  public boolean hasSource() {
    var ret = false;
    for (var i = 0; i < nrOfBits; i++) ret |= sourceList.get(i).NrOfConnections() > 0;
    return ret;
  }

  public boolean HasTunnel() {
    return tunnelNames.size() != 0;
  }

  public void InitializeSourceSinks() {
    sourceList.clear();
    sinkList.clear();
    sourceNetsList.clear();
    sinkNetsList.clear();
    for (var i = 0; i < nrOfBits; i++) {
      sourceList.add(new ConnectionPointArray());
      sinkList.add(new ConnectionPointArray());
      sourceNetsList.add(new ConnectionPointArray());
      sinkNetsList.add(new ConnectionPointArray());
    }
  }

  public boolean isBus() {
    return nrOfBits > 1;
  }

  public boolean isEmpty() {
    return myPoints.isEmpty();
  }

  public boolean isForcedRootNet() {
    return requiresToBeRoot;
  }

  public boolean isRootNet() {
    return (myParent == null) || requiresToBeRoot;
  }

  public boolean merge(Net TheNet) {
    if (TheNet.BitWidth() == nrOfBits) {
      myPoints.addAll(TheNet.getPoints());
      segments.addAll(TheNet.getWires());
      tunnelNames.addAll(TheNet.getTunnelNames());
      return true;
    }
    return false;
  }

  public boolean setWidth(int Width) {
    if ((nrOfBits > 0) && (Width != nrOfBits)) return false;
    nrOfBits = Width;
    return true;
  }

  public boolean setParent(Net Parent) {
    if (requiresToBeRoot) return false;
    if (Parent == null) return false;
    if (myParent != null) return false;
    myParent = Parent;
    return true;
  }

  public Set<String> getTunnelNames() {
    return this.tunnelNames;
  }
}
