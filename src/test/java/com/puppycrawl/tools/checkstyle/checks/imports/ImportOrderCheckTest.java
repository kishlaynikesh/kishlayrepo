////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2021 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.checks.imports;

import static com.google.common.truth.Truth.assertWithMessage;
import static com.puppycrawl.tools.checkstyle.checks.imports.ImportOrderCheck.MSG_ORDERING;
import static com.puppycrawl.tools.checkstyle.checks.imports.ImportOrderCheck.MSG_SEPARATED_IN_GROUP;
import static com.puppycrawl.tools.checkstyle.checks.imports.ImportOrderCheck.MSG_SEPARATION;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.antlr.v4.runtime.CommonToken;
import org.junit.jupiter.api.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.internal.utils.TestUtil;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

public class ImportOrderCheckTest extends AbstractModuleTestSupport {

    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/checks/imports/importorder";
    }

    /* Additional test for jacoco, since valueOf()
     * is generated by javac and jacoco reports that
     * valueOf() is uncovered.
     */
    @Test
    public void testImportOrderOptionValueOf() {
        final ImportOrderOption option = ImportOrderOption.valueOf("TOP");
        assertEquals(ImportOrderOption.TOP, option, "Invalid valueOf result");
    }

    @Test
    public void testDefault() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_ORDERING, "java.awt.Dialog"),
            "25:1: " + getCheckMessage(MSG_ORDERING, "javax.swing.JComponent"),
            "27:1: " + getCheckMessage(MSG_ORDERING, "java.io.File"),
            "29:1: " + getCheckMessage(MSG_ORDERING, "java.io.IOException"),
            "34:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP,
                    "sun.tools.util.ModifierFilter.ALL_ACCESS"),
        };

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrder1.java"), expected);
    }

    @Test
    public void testWrongSequenceInNonStaticImports() throws Exception {

        final String[] expected = {
            "19:1: " + getCheckMessage(MSG_ORDERING,
                    "java.util.HashMap"),
        };

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderNonStaticWrongSequence.java"), expected);
    }

    @Test
    public void testMultilineImport() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderMultiline.java"), expected);
    }

    @Test
    public void testGroups() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_ORDERING, "java.awt.Dialog"),
            "29:1: " + getCheckMessage(MSG_ORDERING, "java.io.IOException"),
            "32:1: " + getCheckMessage(MSG_ORDERING, "javax.swing.WindowConstants.*"),
            "34:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP,
                    "sun.tools.util.ModifierFilter.ALL_ACCESS"),
        };

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrder2.java"), expected);
    }

    @Test
    public void testGroupsRegexp() throws Exception {
        final String[] expected = {
            "27:1: " + getCheckMessage(MSG_ORDERING, "java.io.File"),
            "34:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP,
                    "sun.tools.util.ModifierFilter.ALL_ACCESS"),
        };

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrder3.java"), expected);
    }

    @Test
    public void testSeparated() throws Exception {
        final String[] expected = {
            "25:1: " + getCheckMessage(MSG_SEPARATION, "javax.swing.JComponent"),
            "27:1: " + getCheckMessage(MSG_SEPARATION, "java.io.File"),
            "32:1: " + getCheckMessage(MSG_ORDERING, "javax.swing.WindowConstants.*"),
        };

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrder4.java"), expected);
    }

    @Test
    public void testStaticImportSeparated() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.lang.Math.cos"),
            "23:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "org.junit.Assert.assertEquals"),
        };

        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticGroupSeparated.java"), expected);
    }

    @Test
    public void testNoGapBetweenStaticImports() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getPath("InputImportOrderNoGapBetweenStaticImports.java"), expected);
    }

    @Test
    public void testSortStaticImportsAlphabeticallyFalse() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getPath("InputImportOrderSortStaticImportsAlphabetically1.java"),
            expected);
    }

    @Test
    public void testSortStaticImportsAlphabeticallyTrue() throws Exception {
        final String[] expected = {
            "20:1: " + getCheckMessage(MSG_ORDERING,
                "javax.xml.transform.TransformerFactory.newInstance"),
            "21:1: " + getCheckMessage(MSG_ORDERING, "java.lang.Math.cos"),
            "22:1: " + getCheckMessage(MSG_ORDERING, "java.lang.Math.abs"),
        };

        verifyWithInlineConfigParser(
                getPath("InputImportOrderSortStaticImportsAlphabetically2.java"),
            expected);
    }

    @Test
    public void testCaseInsensitive() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getPath("InputImportOrderCaseInsensitive.java"), expected);
    }

    @Test
    public void testContainerCaseInsensitive() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderEclipseStaticCaseSensitive.java"),
            expected);
    }

    @Test
    public void testSimilarGroupPattern() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderSimilarGroupPattern.java"),
            expected);
    }

    @Test
    public void testInvalidOption() throws Exception {

        try {
            final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

            verifyWithInlineConfigParser(
                    getPath("InputImportOrder_Top1.java"), expected);
            assertWithMessage("exception expected").fail();
        }
        catch (CheckstyleException ex) {
            assertEquals(
                    "cannot initialize module com.puppycrawl.tools.checkstyle.TreeWalker - "
                        + "cannot initialize module com.puppycrawl.tools.checkstyle.checks"
                        + ".imports.ImportOrderCheck - "
                        + "Cannot set property 'option' to 'invalid_option'",
                    ex.getMessage(), "Invalid exception message");
        }
    }

    @Test
    public void testTop() throws Exception {
        final String[] expected = {
            "23:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.awt.Button"),
            "28:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.io.IOException"),
            "31:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "javax.swing.JComponent"),
            "34:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.io.File.*"),
            "34:1: " + getCheckMessage(MSG_ORDERING, "java.io.File.*"),
        };

        verifyWithInlineConfigParser(
                getPath("InputImportOrder_Top2.java"), expected);
    }

    @Test
    public void testAbove() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_ORDERING, "java.awt.Button.ABORT"),
            "24:1: " + getCheckMessage(MSG_ORDERING, "java.awt.Dialog"),
            "29:1: " + getCheckMessage(MSG_ORDERING, "java.io.File"),
            "29:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.io.File"),
            "30:1: " + getCheckMessage(MSG_ORDERING, "java.io.File.createTempFile"),
        };

        verifyWithInlineConfigParser(
                getPath("InputImportOrder_Above.java"), expected);
    }

    @Test
    public void testInFlow() throws Exception {
        final String[] expected = {
            "22:1: " + getCheckMessage(MSG_ORDERING, "java.awt.Dialog"),
            "25:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "javax.swing.JComponent"),
            "27:1: " + getCheckMessage(MSG_ORDERING,
                     "javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE"),
            "28:1: " + getCheckMessage(MSG_ORDERING, "javax.swing.WindowConstants.*"),
            "29:1: " + getCheckMessage(MSG_ORDERING, "javax.swing.JTable"),
            "31:1: " + getCheckMessage(MSG_ORDERING, "java.io.File.createTempFile"),
            "31:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.io.File.createTempFile"),
            "32:1: " + getCheckMessage(MSG_ORDERING, "java.io.File"),
        };

        verifyWithInlineConfigParser(
                getPath("InputImportOrder_InFlow.java"), expected);
    }

    @Test
    public void testUnder() throws Exception {
        // is default (testDefault)
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_ORDERING, "java.awt.Dialog"),
            "27:1: " + getCheckMessage(MSG_ORDERING, "java.awt.Button.ABORT"),
            "29:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.io.File.createTempFile"),
            "30:1: " + getCheckMessage(MSG_ORDERING, "java.io.File"),
        };

        verifyWithInlineConfigParser(
                getPath("InputImportOrder_Under.java"), expected);
    }

    @Test
    public void testBottom() throws Exception {
        final String[] expected = {
            "24:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.io.IOException"),
            "27:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "javax.swing.JComponent"),
            "30:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.io.File.*"),
            "31:1: " + getCheckMessage(MSG_ORDERING, "java.io.File"),
            "33:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.io.File.createTempFile"),
            "37:1: " + getCheckMessage(MSG_ORDERING, "java.io.Reader"),
            "37:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.io.Reader"),
        };

        verifyWithInlineConfigParser(
                getPath("InputImportOrder_Bottom.java"), expected);
    }

    @Test
    public void testGetGroupNumber() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderGetGroupNumber.java"), expected);
    }

    @Test
    public void testHonorsTokenProperty() throws Exception {
        final String[] expected = {
            "22:1: " + getCheckMessage(MSG_ORDERING, "java.awt.Button"),
        };

        verifyWithInlineConfigParser(
                getPath("InputImportOrder_HonorsTokensProperty.java"), expected);
    }

    @Test
    public void testWildcard() throws Exception {
        final String[] expected = {
            "25:1: " + getCheckMessage(MSG_ORDERING, "javax.crypto.Cipher"),
        };

        verifyWithInlineConfigParser(
                getPath("InputImportOrder_Wildcard.java"), expected);
    }

    @Test
    public void testWildcardUnspecified() throws Exception {
        final String[] expected = {
            "23:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP,
                "com.puppycrawl.tools.checkstyle.checks.imports.importorder.InputImportOrderBug"),
        };

        verifyWithInlineConfigParser(
                getPath("InputImportOrder_WildcardUnspecified.java"), expected);
    }

    @Test
    public void testNoFailureForRedundantImports() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWithInlineConfigParser(
                getPath("InputImportOrder_NoFailureForRedundantImports.java"),
            expected);
    }

    @Test
    public void testStaticGroupsAlphabeticalOrder() throws Exception {
        final String[] expected = {
            "22:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "org.antlr.v4.runtime.*"),
            "24:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.util.Set"),
        };

        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticGroupOrder1.java"), expected);
    }

    @Test
    public void testStaticGroupsOrder() throws Exception {
        final String[] expected = {
            "22:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "org.antlr.v4.runtime.*"),
            "24:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.util.Set"),
        };
        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticGroupOrder2.java"), expected);
    }

    @Test
    public void testStaticGroupsAlphabeticalOrderBottom() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.util.Set"),
            "23:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.lang.Math.PI"),
        };
        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticGroupOrderBottom1.java"), expected);
    }

    @Test
    public void testStaticGroupsAlphabeticalOrderBottomNegative() throws Exception {
        final String[] expected = {
            "24:1: " + getCheckMessage(MSG_ORDERING, "java.util.Set"),
        };
        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticGroupOrderBottom_Negative1.java"),
            expected);
    }

    /**
     * Tests that a non-static import after a static import correctly gives an
     * error if order=bottom.
     */
    @Test
    public void testStaticGroupsAlphabeticalOrderTopNegative() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_ORDERING, "java.lang.Math.PI"),
        };
        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticGroupOrderBottom_Negative2.java"),
            expected);
    }

    /**
     * Tests that a non-static import before a static import correctly gives an
     * error if order=top.
     */
    @Test
    public void testStaticGroupsAlphabeticalOrderBottomNegative2() throws Exception {
        final String[] expected = {
            "24:1: " + getCheckMessage(MSG_ORDERING, "java.util.Set"),
        };
        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticGroupOrderBottom_Negative3.java"),
            expected);
    }

    @Test
    public void testStaticGroupsOrderBottom() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.util.Set"),
            "23:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.lang.Math.PI"),
        };
        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticGroupOrderBottom2.java"), expected);
    }

    @Test
    public void testImportReception() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWithInlineConfigParser(
                getPath("InputImportOrderRepetition.java"), expected);
    }

    @Test
    public void testStaticImportReceptionTop() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticRepetition1.java"), expected);
    }

    @Test
    public void testStaticImportReception() throws Exception {
        final String[] expected = {
            "20:1: " + getCheckMessage(MSG_SEPARATION, "org.antlr.v4.runtime.CommonToken.*"),
            "23:1: " + getCheckMessage(MSG_ORDERING, "java.util.Set"),
        };
        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticRepetition2.java"), expected);
    }

    @Test
    public void testStaticGroupsOrderAbove() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.util.Set"),
            "23:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.lang.Math.PI"),
            "23:1: " + getCheckMessage(MSG_ORDERING, "java.lang.Math.PI"),
            "24:1: " + getCheckMessage(MSG_ORDERING, "org.antlr.v4.runtime.Recognizer.EOF"),
        };
        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticGroupOrderBottom3.java"), expected);
    }

    @Test
    public void testStaticOnDemandGroupsOrder() throws Exception {
        final String[] expected = {
            "22:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "org.antlr.v4.runtime.*"),
            "24:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.util.Set"),
            "25:1: " + getCheckMessage(MSG_ORDERING, "org.junit.Test"),
        };
        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticOnDemandGroupOrder1.java"), expected);
    }

    @Test
    public void testStaticOnDemandGroupsAlphabeticalOrder() throws Exception {
        final String[] expected = {
            "22:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "org.antlr.v4.runtime.*"),
            "24:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.util.Set"),
            "25:1: " + getCheckMessage(MSG_ORDERING, "org.junit.Test"),
        };
        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticOnDemandGroupOrder2.java"), expected);
    }

    @Test
    public void testStaticOnDemandGroupsOrderBottom() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.util.Set"),
            "23:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.lang.Math.*"),
        };
        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticOnDemandGroupOrderBottom1.java"),
            expected);
    }

    @Test
    public void testStaticOnDemandGroupsAlphabeticalOrderBottom() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.util.Set"),
            "23:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.lang.Math.*"),
        };
        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticOnDemandGroupOrderBottom2.java"),
            expected);
    }

    @Test
    public void testStaticOnDemandGroupsOrderAbove() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.util.Set"),
            "23:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "java.lang.Math.*"),
            "23:1: " + getCheckMessage(MSG_ORDERING, "java.lang.Math.*"),
            "24:1: " + getCheckMessage(MSG_ORDERING, "org.antlr.v4.runtime.CommonToken.*"),
        };
        verifyWithInlineConfigParser(
                getPath("InputImportOrderStaticOnDemandGroupOrderBottom3.java"),
            expected);
    }

    @Test
    public void testGroupWithSlashes() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(ImportOrderCheck.class);
        checkConfig.addProperty("groups", "/^javax");

        try {
            final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

            verify(checkConfig, getPath("InputImportOrder5.java"), expected);
            assertWithMessage("exception expected").fail();
        }
        catch (CheckstyleException ex) {
            assertEquals("cannot initialize module com.puppycrawl.tools.checkstyle.TreeWalker - "
                        + "cannot initialize module com.puppycrawl.tools.checkstyle.checks"
                        + ".imports.ImportOrderCheck - "
                        + "Cannot set property 'groups' to '/^javax'",
                    ex.getMessage(), "Invalid exception message");
            assertEquals("Invalid group: /^javax",
                    ex.getCause().getCause().getCause().getCause().getMessage(),
                    "Invalid exception message");
        }
    }

    @Test
    public void testGroupWithDot() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_ORDERING, "java.awt.Dialog"),
            "23:1: " + getCheckMessage(MSG_ORDERING, "javax.swing.JComponent"),
        };
        verifyWithInlineConfigParser(
                getPath("InputImportOrder_DotPackageName.java"),
            expected);
    }

    @Test
    public void testMultiplePatternMatches() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "org.*"),
        };

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrder_MultiplePatternMatches1.java"),
            expected);
    }

    /**
     * This test requires reflection to insert an unsupported option in the check to cover the
     * exception that gets thrown when a unsupported option is used. The field has a value by
     * default and the setter for the property will throw it's own exception when an unsupported
     * option is given, so there is no other way to cover this code.
     */
    @Test
    public void testVisitTokenSwitchReflection() {
        // Create mock ast
        final DetailAstImpl astImport = mockAST(TokenTypes.IMPORT, "import", 0, 0);
        final DetailAstImpl astIdent = mockAST(TokenTypes.IDENT, "myTestImport", 0, 0);
        astImport.addChild(astIdent);
        final DetailAST astSemi = mockAST(TokenTypes.SEMI, ";", 0, 0);
        astIdent.addNextSibling(astSemi);

        // Set unsupported option
        final ImportOrderCheck mock = new ImportOrderCheck();
        TestUtil.setInternalState(mock, "option", null);

        // expecting IllegalStateException
        try {
            mock.visitToken(astImport);
            assertWithMessage("An exception is expected").fail();
        }
        catch (IllegalStateException ex) {
            assertWithMessage("invalid exception message")
                    .that(ex.getMessage().endsWith(": null"))
                    .isTrue();
        }
    }

    /**
     * Creates MOCK lexical token and returns AST node for this token.
     *
     * @param tokenType type of token
     * @param tokenText text of token
     * @param tokenRow token position in a file (row)
     * @param tokenColumn token position in a file (column)
     * @return AST node for the token
     */
    private static DetailAstImpl mockAST(final int tokenType, final String tokenText,
            final int tokenRow, final int tokenColumn) {
        final CommonToken tokenImportSemi = new CommonToken(tokenType, tokenText);
        tokenImportSemi.setLine(tokenRow);
        tokenImportSemi.setCharPositionInLine(tokenColumn);
        final DetailAstImpl astSemi = new DetailAstImpl();
        astSemi.initialize(tokenImportSemi);
        return astSemi;
    }

    @Test
    public void testEclipseDefaultPositive() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrder_EclipseDefaultPositive.java"), expected);
    }

    @Test
    public void testStaticImportEclipseRepetition() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderEclipseStaticRepetition.java"), expected);
    }

    @Test
    public void testEclipseDefaultNegative() throws Exception {
        final String[] expected = {
            "28:1: " + getCheckMessage(MSG_SEPARATION, "javax.swing.JComponent"),
            "33:1: " + getCheckMessage(MSG_ORDERING, "org.junit.Test"),
        };

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrder_EclipseDefaultNegative.java"), expected);
    }

    @Test
    public void testUseContainerOrderingForStaticTrue() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderEclipseStatic1.java"), expected);
    }

    @Test
    public void testUseContainerOrderingForStaticFalse() throws Exception {
        final String[] expected = {
            "22:1: " + getCheckMessage(MSG_ORDERING,
                "io.netty.handler.codec.http.HttpHeaders.Names.addDate"),
        };
        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderEclipseStatic2.java"), expected);
    }

    @Test
    public void testUseContainerOrderingForStaticTrueCaseSensitive() throws Exception {
        final String[] expected = {
            "23:1: " + getCheckMessage(MSG_ORDERING,
                "io.netty.handler.codec.http.HttpHeaders.Names.DATE"),
            };
        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderEclipseStatic3.java"), expected);
    }

    @Test
    public void testUseContainerOrderingForStatic() throws Exception {
        final String[] expected = {
            "22:1: " + getCheckMessage(MSG_ORDERING,
                    "io.netty.handler.Codec.HTTP.HttpHeaders.tmp.same"),
            "23:1: " + getCheckMessage(MSG_ORDERING,
                    "io.netty.handler.Codec.HTTP.HttpHeaders.TKN.same"),
        };
        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderContainerOrdering.java"),
                expected);
    }

    @Test
    public void testImportGroupsRedundantSeparatedInternally() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_SEPARATED_IN_GROUP, "org.*"),
        };
        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrder_MultiplePatternMatches2.java"),
                expected);
    }

    @Test
    public void testStaticGroupsAbove() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderStaticGroupsAbove.java"),
                expected);
    }

    @Test
    public void testStaticGroupsBottom() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderStaticGroupsBottom.java"),
                expected);
    }

    @Test
    public void testStaticGroupsBottomSeparated() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderStaticGroupsBottomSeparated.java"), expected);
    }

    @Test
    public void testStaticGroupsInflow() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderStaticGroupsInflow.java"),
                expected);
    }

    @Test
    public void testStaticGroupsNegative() throws Exception {
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_ORDERING, "org.junit.Assert.fail"),
            "23:1: " + getCheckMessage(MSG_ORDERING, "org.infinispan.test.TestingUtil.extract"),
        };

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderStaticGroupsNegative.java"),
                expected);
    }

    @Test
    public void testStaticGroupsTop() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderStaticGroupsTop.java"),
                expected);
    }

    @Test
    public void testStaticGroupsTopSeparated() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderStaticGroupsTopSeparated.java"),
                expected);
    }

    @Test
    public void testStaticGroupsUnordered() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(
                getNonCompilablePath("InputImportOrderStaticGroupsUnordered.java"),
                expected);
    }

}
