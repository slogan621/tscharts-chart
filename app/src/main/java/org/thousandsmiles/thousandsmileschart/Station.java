/*
 * (C) Copyright Syd Logan 2017-2019
 * (C) Copyright Thousand Smiles Foundation 2017-2019
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

public class Station {
    private int m_station;
    private String m_name;
    int m_selector;
    int m_unvisitedSelector;

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

    public void setUnvisitedSelector(int id) {m_unvisitedSelector = id;}

    public int getSelector()
    {
        return m_selector;
    }

    public int getUnvisitedSelector()
    {
        return m_unvisitedSelector;
    }

    public void setName(String name)
    {
        m_name = name;
    }

    public String getName()
    {
        return m_name;
    }
}
