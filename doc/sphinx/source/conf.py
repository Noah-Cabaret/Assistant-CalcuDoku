# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information

project = 'Assistant-CalcuDoku'
author = 'NTH'
copyright = '2026, l\'équipe d\'Assistant-CalcuDoku'

release = '0.0.0'
version = '0'

# -- General configuration ---------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

extensions = [
    'sphinx.ext.todo'
]

templates_path = ['_templates']
exclude_patterns = []

language = 'fr'

primary_domain = None

show_authors = True
smartquotes = False


# -- Options for object signatures --

add_function_parentheses = True
maximum_signature_line_length = 50


# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output

html_theme = 'sphinx_book_theme'
html_static_path = ['_static']

html_title = 'Manuel utilisateur Assistant-CalcuDoku'
short_title = 'Manuel Assistant-CalcuDoku'

html_copy_source = True
html_show_sourcelink = True

html_show_sphinx = True


# -- options for latex output --

latex_engine = 'pdflatex'
latex_show_pagerefs = True
latex_show_urls = 'footnote'

