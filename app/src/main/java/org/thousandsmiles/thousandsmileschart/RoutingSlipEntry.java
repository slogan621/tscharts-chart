/*
 * (C) Copyright Syd Logan 2017
 * (C) Copyright Thousand Smiles Foundation 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thousandsmiles.thousandsmileschart;

public class RoutingSlipEntry {
    private int m_station;
    private String m_name;
    private boolean m_visited;
    private int m_id;
    private int m_selector;

    public RoutingSlipEntry() {
    }

    public RoutingSlipEntry(RoutingSlipEntry rhs) {
        this.m_station = rhs.m_station;
        this.m_name = rhs.m_name;
        this.m_visited = rhs.m_visited;
        this.m_selector = rhs.m_selector;
        this.m_id = rhs.m_id;
    }

    public void setId(int id)
    {
        m_id = id;
    }

    public int getId()
    {
        return m_id;
    }

    public void setStation(int id)
    {
        m_station = id;
    }

    public int getStation()
    {
        return m_station;
    }

    public void setSelector(int id)
    {
        m_selector = id;
    }

    public int getSelector()
    {
        return m_selector;
    }

    public void setName(String name)
    {
        m_name = name;
    }

    public String getName()
    {
        String name = SessionSingleton.getInstance().getStationNameTranslated(m_name);
        return name;
    }

    public void setVisited(boolean visited)
    {
        m_visited = visited;
    }

    public boolean getVisited()
    {
        return m_visited;
    }
}
