/**
 * Amazon Fire TV Development Resources
 *
 * Copyright 2004-2014 Amazon.com, Inc. or its affiliates.  All Rights Reserved.
 
 * These materials are licensed as "Program Materials" under the Program Materials 
 * License Agreement (the "License") of the Amazon Mobile App Distribution program, 
 * which is available at https://developer.amazon.com/sdk/pml.html.  See the License 
 * for the specific language governing permissions and limitations under the License.
 *
 * These materials are distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.sample.tom.asbuilibrary.list.adapter;

import java.util.List;

// only load the first three until told to increase the page size and act like a normal PagingCarouselAdapter
abstract public class BaseStackCarouselAdapter<IdentifierType, DataType> extends BasePagingCarouselAdapter<IdentifierType, DataType>
{
    public static final int STACK_SIZE = 3;
    
    public BaseStackCarouselAdapter(List<IdentifierType> list)
    {
        super(list);
        
        setPageSize(STACK_SIZE);
    }
}
