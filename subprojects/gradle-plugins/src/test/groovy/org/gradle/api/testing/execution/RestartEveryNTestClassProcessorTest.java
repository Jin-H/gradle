/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.testing.execution;

import org.gradle.api.testing.TestClassProcessor;
import org.gradle.api.testing.TestClassProcessorFactory;
import org.gradle.api.testing.fabric.TestClassRunInfo;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class RestartEveryNTestClassProcessorTest {
    private final JUnit4Mockery context = new JUnit4Mockery();
    private final TestClassProcessorFactory factory = context.mock(TestClassProcessorFactory.class);
    private final TestClassProcessor delegate = context.mock(TestClassProcessor.class);
    private final RestartEveryNTestClassProcessor processor = new RestartEveryNTestClassProcessor(factory, 2);
    private final TestClassRunInfo test1 = context.mock(TestClassRunInfo.class, "test1");
    private final TestClassRunInfo test2 = context.mock(TestClassRunInfo.class, "test2");
    private final TestClassRunInfo test3 = context.mock(TestClassRunInfo.class, "test3");

    @Test
    public void onFirstTestCreatesDelegateProcessor() {
        context.checking(new Expectations() {{
            one(factory).create();
            will(returnValue(delegate));

            one(delegate).processTestClass(test1);
        }});

        processor.processTestClass(test1);
    }

    @Test
    public void onNthTestEndsProcessingOnDelegateProcessor() {
        context.checking(new Expectations() {{
            one(factory).create();
            will(returnValue(delegate));

            one(delegate).processTestClass(test1);
            one(delegate).processTestClass(test2);
            one(delegate).endProcessing();
        }});

        processor.processTestClass(test1);
        processor.processTestClass(test2);
    }

    @Test
    public void onNPlus1TestCreatesNewDelegateProcessor() {
        context.checking(new Expectations() {{
            one(factory).create();
            will(returnValue(delegate));

            one(delegate).processTestClass(test1);
            one(delegate).processTestClass(test2);
            one(delegate).endProcessing();

            TestClassProcessor delegate2 = context.mock(TestClassProcessor.class, "delegate2");

            one(factory).create();
            will(returnValue(delegate2));

            one(delegate2).processTestClass(test3);
        }});

        processor.processTestClass(test1);
        processor.processTestClass(test2);
        processor.processTestClass(test3);
    }

    @Test
    public void onEndOfProcessingEndsProcessingOnDelegateProcessor() {
        context.checking(new Expectations() {{
            one(factory).create();
            will(returnValue(delegate));

            one(delegate).processTestClass(test1);
            one(delegate).endProcessing();
        }});

        processor.processTestClass(test1);
        processor.endProcessing();
    }

    @Test
    public void onEndOfProcessingDoesNothingWhenNoTestsReceived() {
        processor.endProcessing();
    }

    @Test
    public void onEndOfProcessingDoesNothingWhenOnNthTest() {
        context.checking(new Expectations() {{
            one(factory).create();
            will(returnValue(delegate));

            one(delegate).processTestClass(test1);
            one(delegate).processTestClass(test2);
            one(delegate).endProcessing();
        }});

        processor.processTestClass(test1);
        processor.processTestClass(test2);
        processor.endProcessing();
    }
}
